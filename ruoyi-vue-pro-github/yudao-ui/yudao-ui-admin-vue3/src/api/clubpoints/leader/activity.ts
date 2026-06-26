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
  level?: number
  registrationDeadline?: string | Date
  cancelDeadlineTime?: string | Date
  checkinStartTime?: string | Date
  checkinEndTime?: string | Date
  checkoutMode?: number
  checkoutStartTime?: string | Date
  checkoutEndTime?: string | Date
  basePoints?: number
  fullExtraPoints?: number
  reason?: string
  attachments?: AttachmentInputVO[]
  [key: string]: any
}

const PREFIX = '/clubpoints/leader/activity'

const withActivityDefaults = (data: LeaderActivitySaveReqVO): LeaderActivitySaveReqVO => ({
  ...data,
  level: data.level ?? 2,
  registrationDeadline: data.registrationDeadline ?? data.startTime,
  cancelDeadlineTime: data.cancelDeadlineTime ?? data.startTime,
  checkinStartTime: data.checkinStartTime ?? data.startTime,
  checkinEndTime: data.checkinEndTime ?? data.endTime,
  checkoutMode: data.checkoutMode ?? 1,
  checkoutStartTime: data.checkoutStartTime ?? data.startTime,
  checkoutEndTime: data.checkoutEndTime ?? data.endTime,
  basePoints: data.basePoints ?? 8,
  fullExtraPoints: data.fullExtraPoints ?? 0
})

export const getLeaderActivityPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<LeaderActivityRespVO>> => {
  return await request.get({ url: `${PREFIX}/page`, params })
}

export const getLeaderActivity = async (id: number): Promise<LeaderActivityRespVO> => {
  return await request.get({ url: `${PREFIX}/get?id=${id}` })
}

export const createLeaderActivity = async (data: LeaderActivitySaveReqVO) => {
  return await request.post({ url: `${PREFIX}/create`, data: withActivityDefaults(data) })
}

export const updateLeaderActivity = async (data: LeaderActivitySaveReqVO) => {
  return await request.put({ url: `${PREFIX}/update`, data: withActivityDefaults(data) })
}

export const submitLeaderActivity = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/submit`, params: { id: data.id } })
}

export const cancelLeaderActivity = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/cancel`, params: { id: data.id, reason: data.reason } })
}
