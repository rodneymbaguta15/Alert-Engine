/**
 * TypeScript mirrors of the backend DTOs in com.alert_engine.dto.
 *
 * These must stay in sync with the Java side manually — when backend DTOs
 * change, update here. An OpenAPI generator could automate this later.
 */

export type AlertDirection = 'ABOVE' | 'BELOW'

export type NotificationChannel = 'IN_APP' | 'EMAIL'

export type DeliveryStatus = 'SENT' | 'FAILED' | 'SUPPRESSED_COOLDOWN'

export interface AlertConfigResponse {
  id: number
  ticker: string
  thresholdPrice: string          // BigDecimal serializes to string — don't use `number`
  direction: AlertDirection
  cooldownSeconds: number
  channels: NotificationChannel[]
  enabled: boolean
  createdAt: string               // ISO Instant string
  updatedAt: string
}

export interface AlertHistoryResponse {
  id: number
  alertConfigId: number
  ticker: string
  triggeredPrice: string
  thresholdPrice: string
  direction: AlertDirection
  channel: NotificationChannel
  deliveryStatus: DeliveryStatus
  errorMessage: string | null
  triggeredAt: string
}

export interface PriceQuoteResponse {
  ticker: string
  currentPrice: string
  previousClose: string | null
  highPrice: string | null
  lowPrice: string | null
  openPrice: string | null
  quoteTimestamp: string | null
  fetchedAt: string
}

export interface CreateAlertRequest {
  ticker: string
  thresholdPrice: string | number   // accept either; axios serializes both fine
  direction: AlertDirection
  cooldownSeconds: number
  channels: NotificationChannel[]
}

export interface UpdateAlertRequest {
  thresholdPrice: string | number
  direction: AlertDirection
  cooldownSeconds: number
  channels: NotificationChannel[]
  enabled: boolean
}

/** Spring Data's Page<T> serialization. */
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number          // current page index
  size: number
  first: boolean
  last: boolean
  empty: boolean
}

/** Incoming WebSocket alert payload — see InAppNotificationChannel.java. */
export interface InAppAlert {
  configId: number
  ticker: string
  currentPrice: number
  thresholdPrice: number
  direction: AlertDirection
  triggeredAt: string
}

/** Standard error response from GlobalExceptionHandler. */
export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors?: { field: string; message: string }[]
}