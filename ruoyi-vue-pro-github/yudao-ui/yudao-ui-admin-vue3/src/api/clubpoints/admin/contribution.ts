import request from '@/config/axios'
import {
  CLUB_POINT_REQUEST_BIZ_TYPES,
  getOrCreateRequestNo,
  resetRequestNo
} from '@/api/clubpoints/shared/requestNo'
import type {
  AttachmentInputVO,
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ReviewReqVO
} from '@/api/clubpoints/shared/types'

export interface AdminContributionRespVO extends ClubPointBaseRespVO {
  clubId: number
  title: string
  status: number
}

export interface AdminContributionDirectCreateReqVO {
  requestNo: string
  userId: number
  points: number
  ruleVersionId: number
  ruleItemCode?: string
  clubId?: number
  reason: string
  attachments?: AttachmentInputVO[]
}

export interface AdminContributionViolationDeductReqVO extends AdminContributionDirectCreateReqVO {
  clubId: number
}

export interface AdminContributionFraudHandleReqVO {
  requestNo: string
  materialId: number
  ruleVersionId: number
  reason: string
  attachments?: AttachmentInputVO[]
}

const PREFIX = '/clubpoints/contribution'

export const getDirectContributionRequestNo = (contextKey: string) => {
  return getOrCreateRequestNo(CLUB_POINT_REQUEST_BIZ_TYPES.DIRECT_CONTRIBUTION, contextKey)
}

export const resetDirectContributionRequestNo = (contextKey: string) => {
  return resetRequestNo(CLUB_POINT_REQUEST_BIZ_TYPES.DIRECT_CONTRIBUTION, contextKey)
}

export const getViolationDeductRequestNo = (contextKey: string) => {
  return getOrCreateRequestNo('VIOLATION_DEDUCT', contextKey)
}

export const getFraudHandleRequestNo = (contextKey: string) => {
  return getOrCreateRequestNo('FRAUD_HANDLE', contextKey)
}

export const getContributionReviewPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminContributionRespVO>> => {
  return await request.get({ url: `${PREFIX}/review-page`, params })
}

export const getContribution = async (id: number): Promise<AdminContributionRespVO> => {
  return await request.get({ url: `${PREFIX}/get?id=${id}` })
}

export const reviewContribution = async (data: ReviewReqVO) => {
  return await request.post({ url: `${PREFIX}/review`, data })
}

export const directCreateContribution = async (data: AdminContributionDirectCreateReqVO) => {
  return await request.post({ url: `${PREFIX}/direct-create`, data })
}

export const violationDeduct = async (data: AdminContributionViolationDeductReqVO) => {
  return await request.post({ url: `${PREFIX}/violation-deduct`, data })
}

export const fraudHandle = async (data: AdminContributionFraudHandleReqVO) => {
  return await request.post({ url: `${PREFIX}/fraud-handle`, data })
}
