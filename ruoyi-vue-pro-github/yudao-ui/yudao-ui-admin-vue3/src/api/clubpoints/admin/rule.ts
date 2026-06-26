import request from '@/config/axios'
import type {
  AttachmentInputVO,
  ClubPointPageParam,
  ClubPointPageResult,
  ClubPointReasonReqVO
} from '@/api/clubpoints/shared/types'

export interface RuleVersionSaveReqVO {
  id?: number
  versionNo?: string
  name: string
  publicityTime?: string | Date
  effectiveTime?: string | Date
  summary?: string
  content?: string
  attachments?: AttachmentInputVO[]
}

export interface RuleVersionRespVO extends RuleVersionSaveReqVO {
  id: number
  status: number
}

export interface RuleItemSaveReqVO {
  id?: number
  ruleVersionId: number
  itemCode: string
  itemName: string
  itemType?: number
  category?: number
  minPoints?: number
  maxPoints?: number
  defaultPoints?: number
  status?: number
  sort?: number
  remark?: string
}

const PREFIX = '/clubpoints/rule'

export const getRuleVersionPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<RuleVersionRespVO>> => {
  return await request.get({ url: `${PREFIX}/page`, params })
}

export const getRuleVersion = async (id: number): Promise<RuleVersionRespVO> => {
  return await request.get({ url: `${PREFIX}/get?id=${id}` })
}

export const createRuleVersion = async (data: RuleVersionSaveReqVO) => {
  return await request.post({ url: `${PREFIX}/create`, data })
}

export const updateRuleVersion = async (data: RuleVersionSaveReqVO) => {
  return await request.put({ url: `${PREFIX}/update`, data })
}

export const copyRuleVersion = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/copy`, data })
}

export const publishRuleVersion = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/publish`, data })
}

export const withdrawRuleVersion = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/withdraw`, data })
}

export const disableRuleVersion = async (data: ClubPointReasonReqVO) => {
  return await request.post({ url: `${PREFIX}/disable`, data })
}

export const getRuleItemList = async (ruleVersionId: number) => {
  return await request.get({ url: `${PREFIX}/item-list?ruleVersionId=${ruleVersionId}` })
}

export const saveRuleItem = async (data: RuleItemSaveReqVO) => {
  return await request.post({ url: `${PREFIX}/item/save`, data })
}
