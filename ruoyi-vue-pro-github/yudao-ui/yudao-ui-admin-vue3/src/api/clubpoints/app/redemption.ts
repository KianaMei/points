import request from '@/config/axios'
import {
  CLUB_POINT_REQUEST_BIZ_TYPES,
  getOrCreateRequestNo,
  resetRequestNo
} from '@/api/clubpoints/shared/requestNo'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO
} from '@/api/clubpoints/shared/types'

export interface AppRedemptionBatchRespVO extends ClubPointBaseRespVO {
  name: string
  status: number
  startTime?: string | Date
  endTime?: string | Date
}

export interface AppRedemptionGiftRespVO extends ClubPointBaseRespVO {
  batchId: number
  name: string
  pointsCost: number
  stockTotal?: number
  stockLocked?: number
  stockUsed?: number
  status: number
}

export interface AppRedemptionApplyReqVO {
  batchId: number
  giftId: number
  quantity: number
  requestNo: string
  remark?: string
}

export interface AppRedemptionApplicationRespVO extends ClubPointBaseRespVO {
  applicationNo?: string
  batchId: number
  giftId: number
  points: number
  status: number
}

export interface AppRedemptionGiftPageReqVO extends ClubPointPageParam {
  batchId: number
}

const PREFIX = '/clubpoints/app/redemption'

export const getRedemptionApplyRequestNo = (contextKey: string) => {
  return getOrCreateRequestNo(CLUB_POINT_REQUEST_BIZ_TYPES.REDEMPTION_APPLY, contextKey)
}

export const resetRedemptionApplyRequestNo = (contextKey: string) => {
  return resetRequestNo(CLUB_POINT_REQUEST_BIZ_TYPES.REDEMPTION_APPLY, contextKey)
}

export const getOpenBatchPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AppRedemptionBatchRespVO>> => {
  return await request.get({ url: `${PREFIX}/batch-page`, params })
}

export const getGiftPage = async (
  params: AppRedemptionGiftPageReqVO
): Promise<ClubPointPageResult<AppRedemptionGiftRespVO>> => {
  return await request.get({ url: `${PREFIX}/gift-page`, params })
}

export const applyRedemption = async (data: AppRedemptionApplyReqVO) => {
  return await request.post({ url: `${PREFIX}/apply`, data })
}

export const cancelRedemption = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/cancel`, data })
}

export const getMyRedemptionPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AppRedemptionApplicationRespVO>> => {
  return await request.get({ url: `${PREFIX}/my-page`, params })
}
