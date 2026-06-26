import request from '@/config/axios'
import type {
  AttachmentInputVO,
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO
} from '@/api/clubpoints/shared/types'

export interface AdminDisputeHandleReqVO extends ClubPointReasonReqVO {
  replyContent: string
  relatedActionType?: number
  relatedTransactionId?: number
  requestNo?: string
  transactionNo?: string
  userId?: number
  userNameSnapshot?: string
  deptIdSnapshot?: number
  deptNameSnapshot?: string
  adjustType?: number
  direction?: number
  points?: number
  issuingClubId?: number
  ruleVersionId?: number
  ruleItemCode?: string
  materialSummary?: string
  attachmentSnapshotJson?: string
  occurredAt?: string | Date
}

export interface AdminAnnualClearReqVO {
  year: number
  reason: string
}

export interface AdminAnnualClearRespVO {
  totalCount?: number
  successCount?: number
  skipCount?: number
  failedCount?: number
}

export interface AdminAnnualRankingGenerateReqVO {
  year: number
  reason?: string
}

export interface AdminBudgetSaveReqVO {
  id?: number
  category: number
  budgetAmountCent: number
  actualAmountCent?: number
  occurDate: string | Date
  sourceType?: number
  sourceId?: number
  handlerUserId?: number
  description?: string
  remark?: string
  reason: string
  attachments?: AttachmentInputVO[]
}

export interface AdminOperationRespVO extends ClubPointBaseRespVO {
  status?: number
}

export interface AdminJobRunRespVO extends ClubPointBaseRespVO {
  taskType?: string
  bizType?: string
  bizId?: number
  runKey?: string
  idempotencyKey?: string
  status?: number
  plannedTime?: string | Date
  startTime?: string | Date
  endTime?: string | Date
  triggerSource?: number
  handlerUserId?: number
  totalCount?: number
  successCount?: number
  skipCount?: number
  failedCount?: number
  retryCount?: number
  nextRetryTime?: string | Date
  errorType?: string
  errorMessage?: string
  resultJson?: string
  manualHandleReason?: string
}

export interface AdminJobRunHandleReqVO {
  id: number
  reason: string
}

const DISPUTE_PREFIX = '/clubpoints/dispute'
const ANNUAL_PREFIX = '/clubpoints/annual'
const BUDGET_PREFIX = '/clubpoints/budget'
const REPORT_PREFIX = '/clubpoints/report'
const JOB_RUN_PREFIX = '/clubpoints/job-run'

export const getAdminDisputePage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminOperationRespVO>> => {
  return await request.get({ url: `${DISPUTE_PREFIX}/page`, params })
}

export const handleDispute = async (data: AdminDisputeHandleReqVO) => {
  return await request.post({ url: `${DISPUTE_PREFIX}/handle`, data })
}

export const clearAnnualPoints = async (data: AdminAnnualClearReqVO): Promise<AdminAnnualClearRespVO> => {
  return await request.post({ url: `${ANNUAL_PREFIX}/clear`, data })
}

export const getAnnualClearingRecordPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${ANNUAL_PREFIX}/clearing-record-page`, params })
}

export const generateAnnualRanking = async (data: AdminAnnualRankingGenerateReqVO) => {
  return await request.post({ url: `${ANNUAL_PREFIX}/ranking-generate`, data })
}

export const getAnnualRankingPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${ANNUAL_PREFIX}/ranking-page`, params })
}

export const suggestAnnualIncentive = async (data: AdminAnnualRankingGenerateReqVO) => {
  return await request.post({ url: `${ANNUAL_PREFIX}/incentive-suggest`, data })
}

export const confirmAnnualIncentive = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${ANNUAL_PREFIX}/incentive-confirm`, data })
}

export const cancelAnnualIncentive = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${ANNUAL_PREFIX}/incentive-cancel`, data })
}

export const getBudgetPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${BUDGET_PREFIX}/page`, params })
}

export const createBudget = async (data: AdminBudgetSaveReqVO) => {
  return await request.post({ url: `${BUDGET_PREFIX}/create`, data })
}

export const updateBudget = async (data: AdminBudgetSaveReqVO) => {
  return await request.put({ url: `${BUDGET_PREFIX}/update`, data })
}

export const disableBudget = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${BUDGET_PREFIX}/disable`, data })
}

export const getReportPointDetailPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${REPORT_PREFIX}/point-detail-page`, params })
}

export const getReportLedgerSummaryPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${REPORT_PREFIX}/ledger-summary-page`, params })
}

export const getReportRedemptionPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${REPORT_PREFIX}/redemption-page`, params })
}

export const getReportClubRankingPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${REPORT_PREFIX}/club-ranking-page`, params })
}

export const getReportBudgetPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${REPORT_PREFIX}/budget-page`, params })
}

export const exportReportExcel = async (params: ClubPointPageParam & { reportType: number }) => {
  return await request.download({ url: `${REPORT_PREFIX}/export-excel`, params })
}

export const getJobRunPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminJobRunRespVO>> => {
  return await request.get({ url: `${JOB_RUN_PREFIX}/page`, params })
}

export const getJobRunDetail = async (id: number): Promise<AdminJobRunRespVO> => {
  return await request.get({ url: `${JOB_RUN_PREFIX}/detail`, params: { id } })
}

export const handleJobRun = async (data: AdminJobRunHandleReqVO) => {
  return await request.post({ url: `${JOB_RUN_PREFIX}/handle`, data })
}
