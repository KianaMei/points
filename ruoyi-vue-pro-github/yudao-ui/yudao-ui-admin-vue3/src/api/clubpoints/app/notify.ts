import request from '@/config/axios'
import qs from 'qs'
import type {
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult
} from '@/api/clubpoints/shared/types'

export interface AppNotifyMessageRespVO extends ClubPointBaseRespVO {
  templateCode?: string
  templateContent: string
  readStatus: boolean
  readTime?: string | Date
}

const PREFIX = '/clubpoints/app/notify'

export const getMyNotifyPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AppNotifyMessageRespVO>> => {
  return await request.get({ url: `${PREFIX}/my-page`, params })
}

export const updateNotifyRead = async (ids: number[]) => {
  return await request.put({
    url: `${PREFIX}/update-read?` + qs.stringify({ ids }, { indices: false })
  })
}
