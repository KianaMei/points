import request from '@/config/axios'
import type { ClubPointDashboardTodoItemVO } from '@/api/clubpoints/shared/types'

export interface AdminDashboardSummaryRespVO {
  pendingActivityReviewCount: number
  pendingContributionReviewCount: number
  pendingRedemptionReviewCount: number
  pendingDisputeCount: number
  todoCount: number
  todoItems: ClubPointDashboardTodoItemVO[]
}

export const getAdminDashboardSummary = async (): Promise<AdminDashboardSummaryRespVO> => {
  return await request.get({ url: '/clubpoints/admin/dashboard/summary' })
}
