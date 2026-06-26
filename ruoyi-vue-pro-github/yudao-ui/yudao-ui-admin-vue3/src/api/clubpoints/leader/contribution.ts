import request from '@/config/axios'
import type {
  AttachmentInputVO,
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO
} from '@/api/clubpoints/shared/types'

export interface LeaderContributionItemReqVO {
  userId: number
  ruleItemCode: string
  points: number
  reason?: string
  materialSummary?: string
}

export interface LeaderContributionSaveReqVO {
  id?: number
  clubId: number
  type?: number
  ruleVersionId: number
  title: string
  description?: string
  reason?: string
  items: LeaderContributionItemReqVO[]
  attachments?: AttachmentInputVO[]
}

export interface LeaderContributionRespVO extends ClubPointBaseRespVO {
  clubId: number
  title: string
  status: number
}

const PREFIX = '/clubpoints/leader/contribution'

export const getLeaderContributionPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<LeaderContributionRespVO>> => {
  return await request.get({ url: `${PREFIX}/page`, params })
}

export const getLeaderContribution = async (id: number): Promise<LeaderContributionRespVO> => {
  return await request.get({ url: `${PREFIX}/get?id=${id}` })
}

export const createLeaderContribution = async (data: LeaderContributionSaveReqVO) => {
  return await request.post({ url: `${PREFIX}/create`, data })
}

export const updateLeaderContribution = async (data: LeaderContributionSaveReqVO) => {
  return await request.put({ url: `${PREFIX}/update`, data })
}

export const submitLeaderContribution = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/submit`, data })
}

export const withdrawLeaderContribution = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/withdraw`, params: { id: data.id, reason: data.reason } })
}
