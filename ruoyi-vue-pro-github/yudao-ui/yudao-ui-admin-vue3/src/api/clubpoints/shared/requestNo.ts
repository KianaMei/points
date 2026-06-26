export const CLUB_POINT_REQUEST_BIZ_TYPES = {
  REDEMPTION_APPLY: 'REDEMPTION_APPLY',
  DIRECT_CONTRIBUTION: 'DIRECT_CONTRIBUTION',
  LEDGER_ADJUST: 'LEDGER_ADJUST'
} as const

export type ClubPointRequestBizType =
  | (typeof CLUB_POINT_REQUEST_BIZ_TYPES)[keyof typeof CLUB_POINT_REQUEST_BIZ_TYPES]
  | string

const DEFAULT_CONTEXT_KEY = 'default'
const activeRequestNos = new Map<string, string>()

const normalizeBizType = (bizType: ClubPointRequestBizType): string => {
  const normalized = `${bizType || ''}`.trim().toUpperCase()
  if (!normalized) {
    throw new Error('requestNo bizType is required')
  }
  return normalized.replace(/[^A-Z0-9_]/g, '_')
}

const buildMapKey = (
  bizType: ClubPointRequestBizType,
  contextKey = DEFAULT_CONTEXT_KEY
): string => {
  return `${normalizeBizType(bizType)}:${contextKey || DEFAULT_CONTEXT_KEY}`
}

const randomPart = (): string => {
  const cryptoObj = globalThis.crypto
  if (cryptoObj?.getRandomValues) {
    const values = new Uint32Array(2)
    cryptoObj.getRandomValues(values)
    return Array.from(values)
      .map((value) => value.toString(36).toUpperCase().padStart(6, '0'))
      .join('')
      .slice(0, 10)
  }
  return Math.random().toString(36).slice(2, 12).toUpperCase().padEnd(10, '0')
}

export const generateRequestNo = (bizType: ClubPointRequestBizType): string => {
  const timestamp = Date.now().toString(36).toUpperCase()
  return `${normalizeBizType(bizType)}_${timestamp}_${randomPart()}`
}

export const getOrCreateRequestNo = (
  bizType: ClubPointRequestBizType,
  contextKey = DEFAULT_CONTEXT_KEY
): string => {
  const key = buildMapKey(bizType, contextKey)
  const cached = activeRequestNos.get(key)
  if (cached) {
    return cached
  }
  const requestNo = generateRequestNo(bizType)
  activeRequestNos.set(key, requestNo)
  return requestNo
}

export const peekRequestNo = (
  bizType: ClubPointRequestBizType,
  contextKey = DEFAULT_CONTEXT_KEY
): string | undefined => {
  return activeRequestNos.get(buildMapKey(bizType, contextKey))
}

export const resetRequestNo = (
  bizType: ClubPointRequestBizType,
  contextKey = DEFAULT_CONTEXT_KEY
): string => {
  const requestNo = generateRequestNo(bizType)
  activeRequestNos.set(buildMapKey(bizType, contextKey), requestNo)
  return requestNo
}

export const clearRequestNo = (
  bizType: ClubPointRequestBizType,
  contextKey = DEFAULT_CONTEXT_KEY
): void => {
  activeRequestNos.delete(buildMapKey(bizType, contextKey))
}

export const clearAllRequestNos = (): void => {
  activeRequestNos.clear()
}
