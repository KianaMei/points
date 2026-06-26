import request from '@/config/axios'
import type {
  AttachmentInputVO,
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO
} from '@/api/clubpoints/shared/types'

export interface LeaderActivityRespVO extends ClubPointBaseRespVO {
  clubId: number
  title: string
  status: number
  settlementStatus?: number
}

export interface LeaderActivitySaveReqVO {
  id?: number
  clubId: number
  title: string
  ruleVersionId: number
  startTime?: string | Date
  endTime?: string | Date
  reason?: string
  attachments?: AttachmentInputVO[]
  [key: string]: any
}

const PREFIX = '/clubpoints/leader/activity'

export const getLeaderActivityPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<LeaderActivityRespVO>> => {
  return await request.get({ url: `${PREFIX}/page`, params })
}

export const getLeaderActivity = async (id: number): Promise<LeaderActivityRespVO> => {
  return await request.get({ url: `${PREFIX}/get?id=${id}` })
}

export const createLeaderActivity = async (data: LeaderActivitySaveReqVO) => {
  return await request.post({ url: `${PREFIX}/create`, data })
}

export const updateLeaderActivity = async (data: LeaderActivitySaveReqVO) => {
  return await request.put({ url: `${PREFIX}/update`, data })
}

export const submitLeaderActivity = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/submit`, data })
}

export const withdrawLeaderActivity = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/withdraw`, data })
}

export const cancelLeaderActivity = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/cancel`, data })
}

export const deleteLeaderActivity = async (id: number) => {
  return await request.delete({ url: `${PREFIX}/delete?id=${id}` })
}
