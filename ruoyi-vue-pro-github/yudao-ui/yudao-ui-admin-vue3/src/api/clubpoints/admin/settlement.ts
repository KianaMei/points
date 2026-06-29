import request from '@/config/axios'
import type {
  ClubPointBaseRespVO,
  ClubPointDateTime,
  ClubPointPageParam,
  ClubPointPageResult
} from '@/api/clubpoints/shared/types'

export interface AdminSettlementPendingActivityPageReqVO extends ClubPointPageParam {
  clubName?: string
  activityTitle?: string
  startTime?: ClubPointDateTime
  endTime?: ClubPointDateTime
}

export interface AdminSettlementPendingActivityRespVO extends ClubPointBaseRespVO {
  clubId: number
  clubCodeSnapshot?: string
  clubNameSnapshot?: string
  title: string
  status: number
  startTime?: ClubPointDateTime
  endTime?: ClubPointDateTime
}

export interface AdminSettlementRunReqVO {
  activityId: number
  reason: string
  force?: boolean
}

export interface AdminSettlementRunPageReqVO extends ClubPointPageParam {
  clubName?: string
  activityTitle?: string
  status?: number
  startTime?: ClubPointDateTime
  endTime?: ClubPointDateTime
}

export interface AdminSettlementRunRespVO extends ClubPointBaseRespVO {
  activityId: number
  clubId?: number
  activityTitle?: string
  clubName?: string
  activityStartTime?: ClubPointDateTime
  activityEndTime?: ClubPointDateTime
  jobRunId?: number
  runKey?: string
  status: number
  settlementTime?: ClubPointDateTime
  configVersionId?: number
  registrationCount?: number
  successCount?: number
  skipCount?: number
  failedCount?: number
  errorMessage?: string
  triggerSource?: number
  operatorUserId?: number
  resultJson?: string
}

export interface AdminSettlementTransactionRespVO extends ClubPointBaseRespVO {
  userId: number
  userNameSnapshot?: string
  direction: number
  points: number
  pointCategory?: number
  sourceType?: number
  sourceTitleSnapshot?: string
  issuingClubId?: number
  issuingClubNameSnapshot?: string
  activityId?: number
  activityTitleSnapshot?: string
  reason?: string
  occurredAt?: ClubPointDateTime
  idempotencyKey?: string
}

export interface AdminSettlementDetailRespVO {
  run: AdminSettlementRunRespVO
  transactions: AdminSettlementTransactionRespVO[]
}

const PREFIX = '/clubpoints/settlement'

export const getPendingActivityPage = async (
  params: AdminSettlementPendingActivityPageReqVO
): Promise<ClubPointPageResult<AdminSettlementPendingActivityRespVO>> => {
  return await request.get({ url: `${PREFIX}/pending-activity-page`, params })
}

export const runSettlement = async (data: AdminSettlementRunReqVO) => {
  return await request.post({ url: `${PREFIX}/run`, data })
}

export const getSettlementRunPage = async (
  params: AdminSettlementRunPageReqVO
): Promise<ClubPointPageResult<AdminSettlementRunRespVO>> => {
  return await request.get({ url: `${PREFIX}/page`, params })
}

export const getSettlementDetail = async (id: number): Promise<AdminSettlementDetailRespVO> => {
  return await request.get({ url: `${PREFIX}/detail`, params: { id } })
}
