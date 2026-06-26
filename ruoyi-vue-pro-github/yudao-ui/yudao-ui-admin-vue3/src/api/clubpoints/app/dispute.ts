import request from '@/config/axios'
import type {
  AttachmentInputVO,
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult
} from '@/api/clubpoints/shared/types'

export interface AppDisputeCreateReqVO {
  targetType: number
  targetId: number
  title: string
  content: string
  attachments?: AttachmentInputVO[]
}

export interface AppDisputeRespVO extends ClubPointBaseRespVO {
  title: string
  status: number
  targetType: number
  targetId: number
  replyContent?: string
}

const PREFIX = '/clubpoints/app/dispute'

export const createDispute = async (data: AppDisputeCreateReqVO) => {
  return await request.post({ url: `${PREFIX}/create`, data })
}

export const getMyDisputePage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AppDisputeRespVO>> => {
  return await request.get({ url: `${PREFIX}/my-page`, params })
}

export const getDispute = async (id: number): Promise<AppDisputeRespVO> => {
  return await request.get({ url: `${PREFIX}/get?id=${id}` })
}
