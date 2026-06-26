import request from '@/config/axios'
import type {
  AttachmentInputVO,
  ClubPointPageParam,
  ClubPointReasonReqVO,
  ReviewReqVO
} from '@/api/clubpoints/shared/types'

export interface AdminActivitySaveReqVO {
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

export interface AdminAttendanceSupplementReqVO {
  registrationId: number
  targetType: number
  occurTime: string | Date
  reason: string
}

export interface AdminAttendanceCorrectReqVO extends AdminAttendanceSupplementReqVO {
  id: number
}

const ACTIVITY_PREFIX = '/clubpoints/activity'
const ATTENDANCE_PREFIX = '/clubpoints/attendance'
const REGISTRATION_PREFIX = '/clubpoints/registration'

export const getAdminActivityPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${ACTIVITY_PREFIX}/page`, params })
}

export const createAdminActivity = async (data: AdminActivitySaveReqVO) => {
  return await request.post({ url: `${ACTIVITY_PREFIX}/create`, data })
}

export const updateAdminActivity = async (data: AdminActivitySaveReqVO) => {
  return await request.put({ url: `${ACTIVITY_PREFIX}/update`, data })
}

export const publishAdminActivity = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${ACTIVITY_PREFIX}/publish`, data })
}

export const reviewAdminActivity = async (data: ReviewReqVO) => {
  return await request.post({ url: `${ACTIVITY_PREFIX}/review`, data })
}

export const cancelAdminActivity = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${ACTIVITY_PREFIX}/cancel`, data })
}

export const deleteAdminActivity = async (id: number) => {
  return await request.delete({ url: `${ACTIVITY_PREFIX}/delete?id=${id}` })
}

export const supplementAdminAttendance = async (data: AdminAttendanceSupplementReqVO) => {
  return await request.post({ url: `${ATTENDANCE_PREFIX}/supplement`, data })
}

export const correctAdminAttendance = async (data: AdminAttendanceCorrectReqVO) => {
  return await request.post({ url: `${ATTENDANCE_PREFIX}/correct`, data })
}

export const markSpecialAbsence = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${REGISTRATION_PREFIX}/mark-special-absence`, data })
}
