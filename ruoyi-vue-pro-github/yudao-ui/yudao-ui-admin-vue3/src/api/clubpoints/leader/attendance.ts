import request from '@/config/axios'
import type { ClubPointPageParam, ClubPointReasonReqVO } from '@/api/clubpoints/shared/types'

export interface LeaderAttendanceSupplementReqVO {
  registrationId: number
  targetType: number
  occurTime: string | Date
  reason: string
}

export interface LeaderAttendanceCorrectReqVO extends LeaderAttendanceSupplementReqVO {
  id: number
}

const REGISTRATION_PREFIX = '/clubpoints/leader/registration'
const ATTENDANCE_PREFIX = '/clubpoints/leader/attendance'

export const getLeaderRegistrationPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${REGISTRATION_PREFIX}/page`, params })
}

export const getLeaderAttendancePage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${ATTENDANCE_PREFIX}/page`, params })
}

export const supplementLeaderAttendance = async (data: LeaderAttendanceSupplementReqVO) => {
  return await request.post({ url: `${ATTENDANCE_PREFIX}/supplement`, data })
}

export const correctLeaderAttendance = async (data: LeaderAttendanceCorrectReqVO) => {
  return await request.post({ url: `${ATTENDANCE_PREFIX}/correct`, data })
}

export const markLeaderSpecialAbsence = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${REGISTRATION_PREFIX}/mark-special-absence`, data })
}
