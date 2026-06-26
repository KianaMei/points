import request from '@/config/axios'
import type { ClubPointDashboardTodoItemVO } from '@/api/clubpoints/shared/types'

export interface AppDashboardSummaryRespVO {
  availablePoints: number
  frozenPoints: number
  totalEarnedPoints: number
  joinedClubCount: number
  registeredActivityCount: number
  pendingRedemptionCount: number
  unreadNotifyCount: number
  todoCount: number
  todoItems: ClubPointDashboardTodoItemVO[]
}

export const getAppDashboardSummary = async (): Promise<AppDashboardSummaryRespVO> => {
  return await request.get({ url: '/clubpoints/app/dashboard/summary' })
}
