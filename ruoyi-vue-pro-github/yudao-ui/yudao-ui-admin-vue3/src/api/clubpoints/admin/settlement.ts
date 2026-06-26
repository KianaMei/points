import request from '@/config/axios'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult
} from '@/api/clubpoints/shared/types'

export interface AdminSettlementRunReqVO {
  activityId: number
  reason: string
}

export interface AdminSettlementRunRespVO extends ClubPointBaseRespVO {
  activityId: number
  status: number
  resultJson?: string
}

const PREFIX = '/clubpoints/settlement'

export const getPendingActivityPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${PREFIX}/pending-activity-page`, params })
}

export const runSettlement = async (data: AdminSettlementRunReqVO) => {
  return await request.post({ url: `${PREFIX}/run`, data })
}

export const getSettlementRunPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminSettlementRunRespVO>> => {
  return await request.get({ url: `${PREFIX}/page`, params })
}

export const getSettlementDetail = async (id: number) => {
  return await request.get({ url: `${PREFIX}/detail?id=${id}` })
}
