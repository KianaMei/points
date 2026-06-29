#!/usr/bin/env node

import http from 'node:http'
import { spawn } from 'node:child_process'
import { existsSync } from 'node:fs'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { setTimeout as sleep } from 'node:timers/promises'

const __dirname = dirname(fileURLToPath(import.meta.url))
const FRONTEND_ROOT = join(__dirname, '..')
const CLUBPOINTS_API_MARKER = '/admin-api/clubpoints/'

const PAGE_ACTION_TIMEOUT_MS = Number(process.env.CLUBPOINTS_E2E_ACTION_TIMEOUT_MS || 20000)
const LOGIN_TIMEOUT_MS = Number(process.env.CLUBPOINTS_E2E_LOGIN_TIMEOUT_MS || 30000)

const roleFlows = [
  {
    role: 'employee',
    credential: {
      username: requireCredential('CLUBPOINTS_E2E_EMPLOYEE_USERNAME'),
      password: requireCredential('CLUBPOINTS_E2E_EMPLOYEE_PASSWORD')
    },
    expectedMenuText: '员工积分中心',
    actions: [
      pageAction('employee ledger summary', '/clubpoints/app/ledger', '我的积分'),
      pageAction('employee club membership', '/clubpoints/app/club', '我的俱乐部'),
      pageAction('employee activity attendance', '/clubpoints/app/activity', '活动报名签到'),
      pageAction('employee redemption', '/clubpoints/app/redemption', '积分兑换'),
      pageAction('employee disputes', '/clubpoints/app/dispute', '我的异议'),
      pageAction('employee notifications', '/clubpoints/app/notify', '我的通知')
    ]
  },
  {
    role: 'leader',
    credential: {
      username: requireCredential('CLUBPOINTS_E2E_LEADER_USERNAME'),
      password: requireCredential('CLUBPOINTS_E2E_LEADER_PASSWORD')
    },
    expectedMenuText: '负责人工作台',
    actions: [
      pageAction('leader dashboard', '/clubpoints/leader/dashboard', '负责人首页'),
      pageAction('leader clubs', '/clubpoints/leader/club', '负责俱乐部'),
      pageAction('leader activities', '/clubpoints/leader/activity', '活动管理'),
      pageAction('leader attendance', '/clubpoints/leader/attendance', '报名与签到'),
      pageAction('leader contributions', '/clubpoints/leader/contribution', '非签到积分材料')
    ]
  },
  {
    role: 'admin',
    credential: {
      username: process.env.CLUBPOINTS_E2E_ADMIN_USERNAME || 'admin',
      password: process.env.CLUBPOINTS_E2E_ADMIN_PASSWORD || 'admin123'
    },
    expectedMenuText: '积分管理后台',
    actions: [
      pageAction('admin dashboard', '/clubpoints/admin/dashboard', '管理员首页'),
      pageAction('admin rules', '/clubpoints/admin/rule', '规则配置'),
      pageAction('admin clubs', '/clubpoints/admin/club', '俱乐部管理'),
      pageAction('admin activities', '/clubpoints/admin/activity', '活动审核与管理'),
      pageAction('admin settlement', '/clubpoints/admin/settlement', '活动积分发放'),
      pageAction('admin ledger accounts', '/clubpoints/admin/ledger/account', '积分账户'),
      pageAction('admin ledger transactions', '/clubpoints/admin/ledger/transaction', '积分流水'),
      pageAction('admin contribution review', '/clubpoints/admin/contribution-review', '非签到材料审核'),
      pageAction('admin contribution direct', '/clubpoints/admin/contribution-direct', '管理员代录'),
      pageAction('admin redemption batches', '/clubpoints/admin/redemption-batch', '兑换批次'),
      pageAction('admin redemption gifts', '/clubpoints/admin/redemption-gift', '礼品维护'),
      pageAction('admin redemption review', '/clubpoints/admin/redemption-application', '兑换审核'),
      pageAction('admin disputes', '/clubpoints/admin/dispute', '异议处理'),
      pageAction('admin annual clearing', '/clubpoints/admin/annual-clearing', '年度清零'),
      pageAction('admin annual ranking', '/clubpoints/admin/annual-ranking', '年度排名与激励'),
      pageAction('admin budget', '/clubpoints/admin/budget', '预算记录'),
      pageAction('admin report', '/clubpoints/admin/report', '报表中心'),
      pageAction('admin audit', '/clubpoints/admin/audit', '审计日志'),
      pageAction('admin job runs', '/clubpoints/admin/job-run', '任务异常处理')
    ]
  }
]

async function main() {
  const config = readConfig()
  const run = createRunState(config)
  const browser = await launchBrowser(config)

  try {
    const page = await connectToFirstPage(config.remoteDebuggingPort)
    run.cdp = page
    await enableCdp(page)
    attachCollectors(page, run)

    for (const flow of roleFlows) {
      await loginAs(page, run, flow)
      const roleResult = { role: flow.role, actions: [] }
      for (const action of flow.actions) {
        roleResult.actions.push(await executePageAction(page, run, flow, action))
      }
      run.roleResults.push(roleResult)
    }

    assertNoPageErrors(run.pageErrors)
    assertNoUnexpectedResponses(run.clubpointsApiRequests)

    const result = {
      status: 'PASS',
      frontendUrl: config.frontendUrl,
      roleResults: run.roleResults,
      pageErrors: run.pageErrors,
      unexpectedClubpointsResponses: []
    }
    console.log(JSON.stringify(result, null, 2))
  } catch (error) {
    const unexpectedClubpointsResponses = computeUnexpectedClubpointsResponses(run.clubpointsApiRequests)
    const result = {
      status: 'FAIL',
      message: error?.message || String(error),
      roleResults: run.roleResults,
      pageErrors: run.pageErrors,
      unexpectedClubpointsResponses,
      clubpointsApiRequests: run.clubpointsApiRequests
    }
    console.error(JSON.stringify(result, null, 2))
    process.exitCode = 1
  } finally {
    await safeClose(run.cdp)
    await browser.close()
  }
}

function pageAction(name, path, expectedText) {
  return { name, path, expectedText, expectedForbiddenUrls: [] }
}

function readConfig() {
  return {
    frontendUrl: trimRightSlash(process.env.CLUBPOINTS_E2E_FRONTEND_URL || 'http://127.0.0.1:8889'),
    remoteDebuggingPort: Number(process.env.CLUBPOINTS_E2E_CDP_PORT || 9229),
    headless: process.env.CLUBPOINTS_E2E_HEADLESS !== 'false',
    chromePath: process.env.CLUBPOINTS_E2E_CHROME_PATH || findChromeExecutable()
  }
}

function createRunState(config) {
  return {
    config,
    cdp: undefined,
    pageErrors: [],
    requestById: new Map(),
    clubpointsApiRequests: [],
    roleResults: []
  }
}

function requireCredential(name) {
  const value = process.env[name]
  if (!value) {
    throw new Error(`${name} is required for M13 three-role E2E`)
  }
  return value
}

async function launchBrowser(config) {
  if (!config.chromePath) {
    throw new Error('Chrome or Edge executable was not found. Set CLUBPOINTS_E2E_CHROME_PATH.')
  }

  const userDataDir = await mkdtemp(join(tmpdir(), 'clubpoints-e2e-'))
  const args = [
    `--remote-debugging-port=${config.remoteDebuggingPort}`,
    `--user-data-dir=${userDataDir}`,
    '--no-first-run',
    '--no-default-browser-check',
    '--disable-extensions',
    '--disable-popup-blocking',
    '--remote-allow-origins=*',
    'about:blank'
  ]
  if (config.headless) {
    args.unshift('--headless=new', '--disable-gpu')
  }

  const child = spawn(config.chromePath, args, {
    cwd: FRONTEND_ROOT,
    stdio: ['ignore', 'pipe', 'pipe'],
    windowsHide: true
  })
  const childExit = new Promise((resolve) => child.once('exit', resolve))
  child.stdout.on('data', (data) => process.stdout.write(`[browser] ${data}`))
  child.stderr.on('data', (data) => process.stderr.write(`[browser] ${data}`))

  await waitForDevTools(config.remoteDebuggingPort)

  return {
    close: async () => {
      if (!child.killed) {
        child.kill()
      }
      await waitForProcessExit(child, childExit)
      await removeUserDataDirWithRetry(userDataDir)
    }
  }
}

async function connectToFirstPage(port) {
  const targets = await httpJson(port, '/json/list')
  const target = targets.find((item) => item.type === 'page') || targets[0]
  if (!target?.webSocketDebuggerUrl) {
    throw new Error('Cannot find Chrome DevTools page target')
  }
  return CdpClient.connect(target.webSocketDebuggerUrl)
}

async function enableCdp(cdp) {
  await cdp.send('Page.enable')
  await cdp.send('Runtime.enable')
  await cdp.send('Network.enable')
}

function attachCollectors(cdp, run) {
  cdp.on('Runtime.exceptionThrown', (event) => {
    run.pageErrors.push({
      text: event.exceptionDetails?.text,
      url: event.exceptionDetails?.url,
      lineNumber: event.exceptionDetails?.lineNumber,
      columnNumber: event.exceptionDetails?.columnNumber
    })
  })
  cdp.on('Network.requestWillBeSent', (event) => {
    run.requestById.set(event.requestId, {
      method: event.request?.method,
      url: event.request?.url
    })
  })
  cdp.on('Network.responseReceived', (event) => {
    const request = run.requestById.get(event.requestId) || {}
    const url = event.response?.url || request.url || ''
    if (!url.includes(CLUBPOINTS_API_MARKER)) {
      return
    }
    run.clubpointsApiRequests.push({
      method: request.method || '?',
      url,
      status: event.response.status,
      mimeType: event.response.mimeType,
      requestId: event.requestId
    })
  })
}

async function loginAs(cdp, run, flow) {
  await clearBrowserSession(cdp, run.config.frontendUrl)
  await navigate(cdp, `${run.config.frontendUrl}/login`)
  await waitForExpression(cdp, 'login inputs', `Boolean(document.querySelector('input[type="password"]'))`)

  const loginAction = beginAction(run, `${flow.role} login`)
  loginAction.beforeSnapshot = await getPageSnapshot(cdp)
  await evaluate(cdp, fillAndSubmitLoginExpression(flow.credential.username, flow.credential.password))
  await waitForExpression(
    cdp,
    `${flow.role} login redirect`,
    `!location.href.includes('/login')`,
    LOGIN_TIMEOUT_MS
  )
  await finishLoginAction(cdp, loginAction)
}

async function executePageAction(cdp, run, flow, action) {
  const actionState = beginAction(run, `${flow.role}: ${action.name}`, action.expectedForbiddenUrls)
  actionState.beforeSnapshot = await getPageSnapshot(cdp)
  await navigate(cdp, `${run.config.frontendUrl}${action.path}`)
  await waitForExpression(
    cdp,
    action.name,
    `location.pathname === ${JSON.stringify(action.path)} && document.body && document.body.innerText.includes(${JSON.stringify(action.expectedText)})`,
    PAGE_ACTION_TIMEOUT_MS
  )
  return finishAction(cdp, run, actionState, action.expectedText)
}

function beginAction(run, name, expectedForbiddenUrls = []) {
  return {
    name,
    startedAt: Date.now(),
    beforeRequestIndex: run.clubpointsApiRequests.length,
    beforeSnapshot: undefined,
    expectedForbiddenUrls,
    clubpointsApiRequests: [],
    visibleResultChanged: false,
    non2xx: false,
    inputRetained: false,
    errorVisible: false
  }
}

async function finishAction(cdp, run, action, expectedText) {
  await waitForActionRequests(run, action)
  const beforeSnapshot = action.beforeSnapshot || { url: '', text: '' }
  const afterSnapshot = await getPageSnapshot(cdp)
  action.clubpointsApiRequests = run.clubpointsApiRequests.slice(action.beforeRequestIndex)
  action.visibleResultChanged =
    afterSnapshot.url !== beforeSnapshot.url ||
    normalizeVisibleText(afterSnapshot.text) !== normalizeVisibleText(beforeSnapshot.text)
  action.non2xx = action.clubpointsApiRequests.some((request) => request.status < 200 || request.status >= 300)
  action.errorVisible = await hasVisibleError(cdp)
  action.inputRetained = await hasRetainedInput(cdp)
  action.durationMs = Date.now() - action.startedAt

  assertActionObserved(action, expectedText)
  assertNoUnexpectedResponses(action.clubpointsApiRequests, action.expectedForbiddenUrls)

  return {
    name: action.name,
    durationMs: action.durationMs,
    clubpointsApiRequests: action.clubpointsApiRequests.map((request) => ({
      method: request.method,
      status: request.status,
      path: new URL(request.url).pathname
    })),
    visibleResultChanged: action.visibleResultChanged
  }
}

async function finishLoginAction(cdp, action) {
  const beforeSnapshot = action.beforeSnapshot || { url: '', text: '' }
  const afterSnapshot = await getPageSnapshot(cdp)
  action.visibleResultChanged =
    afterSnapshot.url !== beforeSnapshot.url ||
    normalizeVisibleText(afterSnapshot.text) !== normalizeVisibleText(beforeSnapshot.text)
  action.durationMs = Date.now() - action.startedAt
  if (!action.visibleResultChanged) {
    throw new Error(`${action.name} did not produce an observable page change`)
  }
}

function assertActionObserved(action, expectedText) {
  if (action.clubpointsApiRequests.length < 1) {
    throw new Error(`${action.name} did not call any ${CLUBPOINTS_API_MARKER} API`)
  }
  if (!action.visibleResultChanged) {
    throw new Error(`${action.name} did not produce an observable page change for ${expectedText}`)
  }
}

function assertNoPageErrors(pageErrors) {
  if (pageErrors.length > 0) {
    throw new Error(`Browser page errors were thrown: ${JSON.stringify(pageErrors)}`)
  }
}

function assertNoUnexpectedResponses(clubpointsApiRequests, expectedForbiddenUrls = []) {
  const unexpectedClubpointsResponses = computeUnexpectedClubpointsResponses(
    clubpointsApiRequests,
    expectedForbiddenUrls
  )
  if (unexpectedClubpointsResponses.length > 0) {
    throw new Error(`Unexpected clubpoints API responses: ${JSON.stringify(unexpectedClubpointsResponses)}`)
  }
}

function computeUnexpectedClubpointsResponses(clubpointsApiRequests, expectedForbiddenUrls = []) {
  return clubpointsApiRequests.filter((response) => {
    if ([404, 405, 500].includes(response.status)) {
      return true
    }
    if (response.status === 403) {
      return !expectedForbiddenUrls.some((part) => response.url.includes(part))
    }
    return false
  })
}

async function assertBusinessFailureRetainsInputAndShowsError(action) {
  if (!action.non2xx) {
    throw new Error(`${action.name} did not exercise a non2xx business failure`)
  }
  if (!action.inputRetained) {
    throw new Error(`${action.name} did not retain form input after failure`)
  }
  if (!action.errorVisible) {
    throw new Error(`${action.name} did not show a visible error after failure`)
  }
}

async function clearBrowserSession(cdp, frontendUrl) {
  if (frontendUrl) {
    await cdp.send('Storage.clearDataForOrigin', {
      origin: new URL(frontendUrl).origin,
      storageTypes: 'cookies,local_storage,session_storage,indexeddb,cache_storage,websql,service_workers'
    })
  } else {
    await evaluate(
      cdp,
      `(() => {
        localStorage.clear();
        sessionStorage.clear();
        return true;
      })()`
    )
  }
  await cdp.send('Network.clearBrowserCookies')
}

async function navigate(cdp, url) {
  await cdp.send('Page.navigate', { url })
  await waitForExpression(
    cdp,
    `document ready for ${url}`,
    `document.readyState === 'interactive' || document.readyState === 'complete'`,
    PAGE_ACTION_TIMEOUT_MS
  )
}

async function waitForActionRequests(run, action) {
  const deadline = Date.now() + PAGE_ACTION_TIMEOUT_MS
  while (Date.now() < deadline) {
    if (run.clubpointsApiRequests.length > action.beforeRequestIndex) {
      await sleep(500)
      return
    }
    await sleep(200)
  }
}

async function waitForExpression(cdp, label, expression, timeoutMs = PAGE_ACTION_TIMEOUT_MS) {
  const deadline = Date.now() + timeoutMs
  let lastError
  while (Date.now() < deadline) {
    try {
      const result = await evaluate(cdp, `Boolean(${expression})`)
      if (result === true) {
        return
      }
    } catch (error) {
      lastError = error
    }
    await sleep(200)
  }
  throw new Error(`Timed out waiting for ${label}${lastError ? `: ${lastError.message}` : ''}`)
}

async function getPageSnapshot(cdp) {
  return evaluate(
    cdp,
    `(() => ({
      url: location.href,
      text: document.body ? document.body.innerText : ''
    }))()`
  )
}

async function hasVisibleError(cdp) {
  return evaluate(
    cdp,
    `(() => {
      const text = document.body ? document.body.innerText : '';
      return /失败|错误|异常|不能为空|未通过|请重试|error|failed/i.test(text);
    })()`
  )
}

async function hasRetainedInput(cdp) {
  return evaluate(
    cdp,
    `(() => Array.from(document.querySelectorAll('input, textarea')).some((item) => item.value && item.value.trim()))()`
  )
}

async function evaluate(cdp, expression) {
  const result = await cdp.send('Runtime.evaluate', {
    expression,
    awaitPromise: true,
    returnByValue: true
  })
  if (result.exceptionDetails) {
    throw new Error(result.exceptionDetails.text || 'Runtime.evaluate failed')
  }
  return result.result?.value
}

function fillAndSubmitLoginExpression(username, password) {
  return `(() => {
    const inputs = Array.from(document.querySelectorAll('input'));
    const isVisibleInput = (item) => {
      const type = (item.getAttribute('type') || 'text').toLowerCase();
      const rect = item.getBoundingClientRect();
      const style = window.getComputedStyle(item);
      return type !== 'checkbox' &&
        type !== 'hidden' &&
        !item.disabled &&
        style.display !== 'none' &&
        style.visibility !== 'hidden' &&
        rect.width > 0 &&
        rect.height > 0;
    };
    const visibleInputs = inputs.filter(isVisibleInput);
    const findByPlaceholder = (text) => visibleInputs.find((item) => {
      const placeholder = item.getAttribute('placeholder') || '';
      return placeholder.includes(text);
    });
    const usernameInput = findByPlaceholder('请输入用户名') || findByPlaceholder('用户名');
    const passwordInput = findByPlaceholder('请输入密码') ||
      visibleInputs.find((item) => (item.getAttribute('type') || '').toLowerCase() === 'password');
    if (!usernameInput || !passwordInput) {
      throw new Error('Login inputs not found');
    }
    const setValue = (element, value) => {
      element.focus();
      element.value = value;
      element.dispatchEvent(new Event('input', { bubbles: true }));
      element.dispatchEvent(new Event('change', { bubbles: true }));
    };
    setValue(usernameInput, ${JSON.stringify(username)});
    setValue(passwordInput, ${JSON.stringify(password)});
    const button = Array.from(document.querySelectorAll('button'))
      .find((item) => /登录|Login|Sign in/i.test(item.innerText));
    if (!button) {
      throw new Error('Login button not found');
    }
    button.click();
    return true;
  })()`
}

async function waitForProcessExit(child, childExit, timeoutMs = 5000) {
  if (child.exitCode !== null || child.signalCode !== null) {
    return
  }
  await Promise.race([childExit, sleep(timeoutMs)])
}

async function removeUserDataDirWithRetry(userDataDir) {
  let lastError
  for (let attempt = 0; attempt < 8; attempt++) {
    try {
      await rm(userDataDir, { recursive: true, force: true })
      return
    } catch (error) {
      lastError = error
      if (!['EBUSY', 'EPERM', 'ENOTEMPTY'].includes(error?.code)) {
        throw error
      }
      await sleep(250 * (attempt + 1))
    }
  }
  throw lastError
}

function normalizeVisibleText(text) {
  return String(text || '').replace(/\s+/g, ' ').trim()
}

function trimRightSlash(value) {
  return String(value).replace(/\/+$/, '')
}

async function waitForDevTools(port) {
  const deadline = Date.now() + 15000
  while (Date.now() < deadline) {
    try {
      await httpJson(port, '/json/list')
      return
    } catch {
      await sleep(200)
    }
  }
  throw new Error(`Chrome DevTools did not open on port ${port}`)
}

function httpJson(port, path) {
  return new Promise((resolve, reject) => {
    const request = http.request(
      {
        hostname: '127.0.0.1',
        port,
        path,
        method: 'GET'
      },
      (response) => {
        let body = ''
        response.setEncoding('utf8')
        response.on('data', (chunk) => {
          body += chunk
        })
        response.on('end', () => {
          try {
            resolve(JSON.parse(body))
          } catch (error) {
            reject(error)
          }
        })
      }
    )
    request.on('error', reject)
    request.end()
  })
}

function findChromeExecutable() {
  const candidates = [
    process.env.CHROME_PATH,
    'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe',
    'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    '/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge',
    '/usr/bin/google-chrome',
    '/usr/bin/google-chrome-stable',
    '/usr/bin/chromium-browser',
    '/usr/bin/chromium',
    '/usr/bin/microsoft-edge'
  ].filter(Boolean)
  return candidates.find((candidate) => existsSync(candidate))
}

async function safeClose(cdp) {
  if (!cdp) {
    return
  }
  try {
    await cdp.close()
  } catch {
    // Closing after browser shutdown is best-effort only.
  }
}

class CdpClient {

  static connect(webSocketDebuggerUrl) {
    return new Promise((resolve, reject) => {
      const socket = new WebSocket(webSocketDebuggerUrl)
      const client = new CdpClient(socket)
      socket.addEventListener('open', () => resolve(client), { once: true })
      socket.addEventListener('error', (event) => reject(event.error || new Error('CDP WebSocket failed')), {
        once: true
      })
    })
  }

  constructor(socket) {
    this.socket = socket
    this.nextId = 1
    this.pending = new Map()
    this.listeners = new Map()

    this.socket.addEventListener('message', (event) => {
      const message = JSON.parse(event.data)
      if (message.id) {
        const pending = this.pending.get(message.id)
        if (!pending) {
          return
        }
        this.pending.delete(message.id)
        if (message.error) {
          pending.reject(new Error(message.error.message || JSON.stringify(message.error)))
        } else {
          pending.resolve(message.result || {})
        }
        return
      }
      if (message.method) {
        const listeners = this.listeners.get(message.method) || []
        for (const listener of listeners) {
          listener(message.params || {})
        }
      }
    })
  }

  send(method, params = {}) {
    const id = this.nextId++
    const payload = JSON.stringify({ id, method, params })
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject })
      this.socket.send(payload)
    })
  }

  on(method, listener) {
    if (!this.listeners.has(method)) {
      this.listeners.set(method, [])
    }
    this.listeners.get(method).push(listener)
  }

  close() {
    this.socket.close()
  }

}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
