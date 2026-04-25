import { useEffect, useRef, useState } from 'react'
import SockJS from 'sockjs-client'
import { Client, type IMessage } from '@stomp/stompjs'

/**
 * Manages a STOMP-over-SockJS connection to the backend and subscribes to a
 * per-user alert topic.
 *
 * Auth: the JWT is sent as the Authorization header on the STOMP CONNECT frame.
 * The server-side WS endpoint is currently permitAll (handshake-level), with
 * subscription-scope guarantees enforced by client-side topic targeting; a future
 * hardening step is to add a STOMP message interceptor that validates this token
 * server-side and rejects subscriptions to other users' topics.
 */

export type WsStatus = 'connecting' | 'connected' | 'disconnected'

interface Options {
  userId: number | undefined
  token: string | null
  onMessage: (payload: unknown) => void
  enabled?: boolean
}

export function useWebSocket({ userId, token, onMessage, enabled = true }: Options) {
  const [status, setStatus] = useState<WsStatus>('disconnected')

  const onMessageRef = useRef(onMessage)
  onMessageRef.current = onMessage

  useEffect(() => {
    // Don't connect unless we have everything we need.
    if (!enabled || !userId || !token) {
      setStatus('disconnected')
      return
    }

    const topic = `/topic/alerts/user/${userId}`

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      // Send the JWT as a STOMP CONNECT header. The server can read this from
      // the StompHeaderAccessor in a ChannelInterceptor (future hardening).
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5_000,
      debug: () => {},
    })

    client.onConnect = () => {
      setStatus('connected')
      client.subscribe(topic, (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body)
          onMessageRef.current(payload)
        } catch (e) {
          console.error('Failed to parse WS message', e, message.body)
        }
      })
    }

    client.onStompError = (frame) => {
      console.error('STOMP error', frame.headers, frame.body)
      setStatus('disconnected')
    }

    client.onWebSocketClose = () => {
      setStatus('disconnected')
    }

    client.onWebSocketError = (e) => {
      console.error('WebSocket error', e)
      setStatus('disconnected')
    }

    setStatus('connecting')
    client.activate()

    return () => {
      client.deactivate().catch(() => {
        /* benign during unmount */
      })
    }
  }, [userId, token, enabled])

  return { status }
}