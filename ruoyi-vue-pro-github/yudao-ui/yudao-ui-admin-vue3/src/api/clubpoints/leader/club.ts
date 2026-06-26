import request from '@/config/axios'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult
} from '@/api/clubpoints/shared/types'

export interface LeaderClubRespVO extends ClubPointBaseRespVO {
  name: string
  code?: string
  status?: number
}

export interface LeaderClubSaveReqVO {
  id: number
  name?: string
  description?: string
  reason?: string
}

export interface LeaderMemberRespVO extends ClubPointBaseRespVO {
  clubId: number
  userId: number
  nickname?: string
  status?: number
}

const CLUB_PREFIX = '/clubpoints/leader/club'
const MEMBER_PREFIX = '/clubpoints/leader/member'
const LEDGER_PREFIX = '/clubpoints/leader/ledger'

export const getMyManagedClubList = async (): Promise<LeaderClubRespVO[]> => {
  return await request.get({ url: `${CLUB_PREFIX}/my-managed-list` })
}

export const getLeaderClub = async (id: number): Promise<LeaderClubRespVO> => {
  return await request.get({ url: `${CLUB_PREFIX}/get?id=${id}` })
}

export const updateLeaderClub = async (data: LeaderClubSaveReqVO) => {
  return await request.put({ url: `${CLUB_PREFIX}/update`, data })
}

export const getLeaderMemberPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<LeaderMemberRespVO>> => {
  return await request.get({ url: `${MEMBER_PREFIX}/page`, params })
}

export const getLeaderMemberPointSummaryPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${LEDGER_PREFIX}/member-summary-page`, params })
}

export const getLeaderLedgerTransactionPage = async (params: ClubPointPageParam) => {
  return await request.get({ url: `${LEDGER_PREFIX}/transaction-page`, params })
}
