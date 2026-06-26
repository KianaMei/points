import request from '@/config/axios'
import type {
  AttachmentInputVO,
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO
} from '@/api/clubpoints/shared/types'

export interface AdminDisputeHandleReqVO extends ClubPointReasonReqVO {
  handleType: number
  adjustPoints?: number
  ruleVersionId?: number
  ruleItemCode?: string
}

export interface AdminAnnualClearReqVO {
  year: number
  reason: string
}

export interface AdminAnnualRankingGenerateReqVO {
  year: number
  reason?: string
}

export interface AdminBudgetSaveReqVO {
  id?: number
  occurDate: string | Date
  amountCent: number
  sourceType?: number
  sourceId?: number
  reason: string
  attachments?: AttachmentInputVO[]
}

export interface AdminOperationRespVO extends ClubPointBaseRespVO {
  status?: number
}

const DISPUTE_PREFIX = '/clubpoints/dispute'
const ANNUAL_PREFIX = '/clubpoints/annual'
const BUDGET_PREFIX = '/clubpoints/budget'
const REPORT_PREFIX = '/clubpoints/report'

export const getAdminDisputePage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminOperationRespVO>> => {
  return await request.get({ url: `${DISPUTE_PREFIX}/page`, params })
}

export const handleDispute = async (data: AdminDisputeHandleReqVO) => {
  return await request.post({ url: `${DISPUTE_PREFIX}/handle`, data })
}

export const clearAnnualPoints = async (data: AdminAnnualClearReqVO) => {
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
