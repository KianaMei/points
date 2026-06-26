import request from '@/config/axios'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO,
  ClubPointStrongConfirmReqVO
} from '@/api/clubpoints/shared/types'

export interface AdminClubRespVO extends ClubPointBaseRespVO {
  name: string
  code?: string
  status: number
}

export interface AdminClubSaveReqVO {
  id?: number
  name: string
  code?: string
  description?: string
  reason?: string
}

export interface AdminClubMemberSaveReqVO {
  clubId: number
  userId: number
  reason?: string
}

const CLUB_PREFIX = '/clubpoints/club'
const MEMBER_PREFIX = '/clubpoints/club-member'
const LEADER_PREFIX = '/clubpoints/club-leader'

export const getClubPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminClubRespVO>> => {
  return await request.get({ url: `${CLUB_PREFIX}/page`, params })
}

export const getClub = async (id: number): Promise<AdminClubRespVO> => {
  return await request.get({ url: `${CLUB_PREFIX}/get?id=${id}` })
}

export const createClub = async (data: AdminClubSaveReqVO) => {
  return await request.post({ url: `${CLUB_PREFIX}/create`, data })
}

export const updateClub = async (data: AdminClubSaveReqVO) => {
  return await request.put({ url: `${CLUB_PREFIX}/update`, data })
}

export const disableClub = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${CLUB_PREFIX}/disable`, data })
}

export const deleteClub = async (data: ClubPointStrongConfirmReqVO) => {
  return await request.delete({ url: `${CLUB_PREFIX}/delete`, data })
}

export const getClubMemberPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${MEMBER_PREFIX}/page`, params })
}

export const addClubMember = async (data: AdminClubMemberSaveReqVO) => {
  return await request.post({ url: `${MEMBER_PREFIX}/add`, data })
}

export const removeClubMember = async (data: AdminClubMemberSaveReqVO) => {
  return await request.post({ url: `${MEMBER_PREFIX}/remove`, data })
}

export const getClubLeaderPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${LEADER_PREFIX}/page`, params })
}

export const assignClubLeader = async (data: AdminClubMemberSaveReqVO) => {
  return await request.post({ url: `${LEADER_PREFIX}/assign`, data })
}

export const removeClubLeader = async (data: AdminClubMemberSaveReqVO) => {
  return await request.post({ url: `${LEADER_PREFIX}/remove`, data })
}
