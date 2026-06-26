import request from '@/config/axios'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO
} from '@/api/clubpoints/shared/types'

export interface AppActivityRespVO extends ClubPointBaseRespVO {
  clubId: number
  title: string
  status: number
  settlementStatus?: number
  startTime?: string | Date
  endTime?: string | Date
}

export interface AppActivityPageReqVO extends ClubPointPageParam {
  clubId?: number
  activityStatus?: number
  keyword?: string
}

export interface AppRegistrationCreateReqVO {
  activityId: number
  reason?: string
}

export interface AppAttendanceCheckReqVO {
  registrationId: number
  clientTime?: string | Date
}

const ACTIVITY_PREFIX = '/clubpoints/app/activity'
const REGISTRATION_PREFIX = '/clubpoints/app/registration'
const ATTENDANCE_PREFIX = '/clubpoints/app/attendance'

export const getAppActivityPage = async (
  params: AppActivityPageReqVO
): Promise<ClubPointPageResult<AppActivityRespVO>> => {
  return await request.get({ url: `${ACTIVITY_PREFIX}/page`, params })
}

export const getAppActivity = async (id: number): Promise<AppActivityRespVO> => {
  return await request.get({ url: `${ACTIVITY_PREFIX}/get?id=${id}` })
}

export const getMyRegistrationPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${REGISTRATION_PREFIX}/my-page`, params })
}

export const createRegistration = async (data: AppRegistrationCreateReqVO) => {
  return await request.post({ url: `${REGISTRATION_PREFIX}/create`, data })
}

export const cancelRegistration = async (data: ClubPointReasonReqVO) => {
  return await request.post({
    url: `${REGISTRATION_PREFIX}/cancel`,
    data: { registrationId: data.id, reason: data.reason }
  })
}

export const checkIn = async (data: AppAttendanceCheckReqVO) => {
  return await request.post({ url: `${ATTENDANCE_PREFIX}/check-in`, data })
}

export const checkOut = async (data: AppAttendanceCheckReqVO) => {
  return await request.post({ url: `${ATTENDANCE_PREFIX}/check-out`, data })
}
