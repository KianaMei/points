import request from '@/config/axios'
import {
  CLUB_POINT_REQUEST_BIZ_TYPES,
  getOrCreateRequestNo,
  resetRequestNo
} from '@/api/clubpoints/shared/requestNo'
import type {
  AttachmentInputVO,
  ClubPointBaseRespVO,
  ClubPointPageParam,
  ClubPointPageResult
} from '@/api/clubpoints/shared/types'

export interface AdminLedgerAccountRespVO extends ClubPointBaseRespVO {
  userId: number
  availablePoints: number
  frozenPoints: number
  totalPoints: number
}

export interface AdminLedgerTransactionRespVO extends ClubPointBaseRespVO {
  userId: number
  direction: number
  points: number
  sourceType?: number
  reason?: string
}

export interface AdminLedgerAdjustReqVO {
  requestNo: string
  userId: number
  direction: number
  points: number
  ruleVersionId: number
  ruleItemCode?: string
  issuingClubId?: number
  reason: string
  attachments?: AttachmentInputVO[]
}

export interface AdminLedgerReverseReqVO {
  transactionId: number
  reason: string
}

const PREFIX = '/clubpoints/ledger'

export const getLedgerAdjustRequestNo = (contextKey: string) => {
  return getOrCreateRequestNo(CLUB_POINT_REQUEST_BIZ_TYPES.LEDGER_ADJUST, contextKey)
}

export const resetLedgerAdjustRequestNo = (contextKey: string) => {
  return resetRequestNo(CLUB_POINT_REQUEST_BIZ_TYPES.LEDGER_ADJUST, contextKey)
}

export const getAccountPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminLedgerAccountRespVO>> => {
  return await request.get({ url: `${PREFIX}/account-page`, params })
}

export const getTransactionPage = async (
  params: ClubPointPageParam
): Promise<ClubPointPageResult<AdminLedgerTransactionRespVO>> => {
  return await request.get({ url: `${PREFIX}/transaction-page`, params })
}

export const adjustLedger = async (data: AdminLedgerAdjustReqVO) => {
  return await request.post({ url: `${PREFIX}/adjust`, data })
}

export const reverseLedger = async (data: AdminLedgerReverseReqVO) => {
  return await request.post({ url: `${PREFIX}/reverse`, data })
}
