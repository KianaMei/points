import request from '@/config/axios'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO
} from '@/api/clubpoints/shared/types'

export interface AppClubRespVO extends ClubPointBaseRespVO {
  name: string
  code?: string
  status?: number
  memberStatus?: number
}

export interface AppClubMemberRespVO extends ClubPointBaseRespVO {
  clubId: number
  userId: number
  nickname?: string
  status?: number
}

export interface AppClubPageReqVO extends ClubPointPageParam {
  keyword?: string
}

export interface AppClubMemberPageReqVO extends ClubPointPageParam {
  clubId: number
  userId?: number
}

const PREFIX = '/clubpoints/app/club'

export const getMyClubList = async (): Promise<AppClubRespVO[]> => {
  return await request.get({ url: `${PREFIX}/my-list` })
}

export const getJoinableClubPage = async (
  params: AppClubPageReqVO
): Promise<ClubPointPageResult<AppClubRespVO>> => {
  return await request.get({ url: `${PREFIX}/joinable-page`, params })
}

export const joinClub = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/join`, data })
}

export const exitClub = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/exit`, data })
}

export const getClubMemberPage = async (
  params: AppClubMemberPageReqVO
): Promise<ClubPointPageResult<AppClubMemberRespVO>> => {
  return await request.get({ url: `${PREFIX}/member-page`, params })
}
