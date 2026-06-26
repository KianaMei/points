import request from '@/config/axios'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO,
  ReviewReqVO
} from '@/api/clubpoints/shared/types'

export interface AdminRedemptionBatchSaveReqVO {
  id?: number
  name: string
  year: number
  ruleVersionId: number
  openTime?: string | Date
  closeTime?: string | Date
  description?: string
  minAvailablePoints: number
  qualifiedCount: number
  includeTieAtCutoff: boolean
  qualificationRule: string
  ruleSnapshotJson?: string
  reason?: string
}

export interface AdminRedemptionBatchRespVO extends ClubPointBaseRespVO {
  year?: number
  name: string
  status: number
  openTime?: string | Date
  closeTime?: string | Date
  description?: string
  minAvailablePoints?: number
  qualifiedCount?: number
  includeTieAtCutoff?: boolean
  qualificationRule?: string
  snapshotGenerated?: boolean
  snapshotGeneratedTime?: string | Date
  ruleVersionId?: number
  ruleSnapshotJson?: string
}

export interface AdminRedemptionGiftSaveReqVO {
  id?: number
  batchId: number
  name: string
  description?: string
  pointsCost: number
  tierMinPoints?: number
  tierMaxPoints?: number
  referenceAmountCent?: number
  stockTotal: number
  imageFileId?: number
  sort: number
  reason?: string
}

export interface AdminRedemptionGiftStatusReqVO extends ClubPointReasonReqVO {
  status: number
}

export interface AdminRedemptionGiftRespVO extends ClubPointBaseRespVO {
  batchId: number
  name: string
  description?: string
  pointsCost: number
  tierMinPoints?: number
  tierMaxPoints?: number
  referenceAmountCent?: number
  stockTotal: number
  stockLocked?: number
  stockUsed?: number
  status: number
  imageFileId?: number
  sort?: number
  giftSnapshotJson?: string
}

export interface AdminRedemptionApplicationRespVO extends ClubPointBaseRespVO {
  applicationNo?: string
  requestNo?: string
  batchId: number
  batchNameSnapshot?: string
  giftId: number
  giftNameSnapshot?: string
  userId: number
  pointsCostSnapshot: number
  quantity: number
  frozenPoints?: number
  status: number
  qualificationRankSnapshot?: number
  applyTime?: string | Date
  cancelTime?: string | Date
  cancelReason?: string
  reviewerUserId?: number
  reviewTime?: string | Date
  reviewReason?: string
  directIssueTime?: string | Date
}

const BATCH_PREFIX = '/clubpoints/redemption-batch'
const GIFT_PREFIX = '/clubpoints/redemption-gift'
const APPLICATION_PREFIX = '/clubpoints/redemption-application'

export const getRedemptionBatchPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminRedemptionBatchRespVO>> => {
  return await request.get({ url: `${BATCH_PREFIX}/page`, params })
}

export const createRedemptionBatch = async (data: AdminRedemptionBatchSaveReqVO) => {
  return await request.post({ url: `${BATCH_PREFIX}/create`, data })
}

export const updateRedemptionBatch = async (data: AdminRedemptionBatchSaveReqVO) => {
  return await request.put({ url: `${BATCH_PREFIX}/update`, data })
}

export const openRedemptionBatch = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${BATCH_PREFIX}/open`, data })
}

export const closeRedemptionBatch = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${BATCH_PREFIX}/close`, data })
}

export const getRedemptionEligibilityPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${BATCH_PREFIX}/eligibility-page`, params })
}

export const getRedemptionGiftPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminRedemptionGiftRespVO>> => {
  return await request.get({ url: `${GIFT_PREFIX}/page`, params })
}

export const createRedemptionGift = async (data: AdminRedemptionGiftSaveReqVO) => {
  return await request.post({ url: `${GIFT_PREFIX}/create`, data })
}

export const updateRedemptionGift = async (data: AdminRedemptionGiftSaveReqVO) => {
  return await request.put({ url: `${GIFT_PREFIX}/update`, data })
}

export const updateRedemptionGiftStatus = async (data: AdminRedemptionGiftStatusReqVO) => {
  return await request.post({ url: `${GIFT_PREFIX}/update-status`, data })
}

export const getRedemptionApplicationPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminRedemptionApplicationRespVO>> => {
  return await request.get({ url: `${APPLICATION_PREFIX}/page`, params })
}

export const reviewRedemptionApplication = async (data: ReviewReqVO) => {
  return await request.post({ url: `${APPLICATION_PREFIX}/review`, data })
}
