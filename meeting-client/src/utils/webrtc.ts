import { getWebSocket } from './websocket'
import { MessageType } from './websocket'

/**
 * ICE 服务器配置
 */
const ICE_SERVERS: RTCIceServer[] = [
  { urls: 'stun:stun.l.google.com:19302' },
  { urls: 'stun:stun1.l.google.com:19302' }
]

/**
 * WebRTC 对等连接管理类
 */
export class WebRTCPeerConnection {
  private pc: RTCPeerConnection | null = null
  private remoteUserId: string
  private localStream: MediaStream | null = null
  private onRemoteStreamCallback?: (stream: MediaStream) => void

  constructor(remoteUserId: string) {
    this.remoteUserId = remoteUserId
    this.initPeerConnection()
  }

  /**
   * 初始化 PeerConnection
   */
  private initPeerConnection(): void {
    this.pc = new RTCPeerConnection({
      iceServers: ICE_SERVERS
    })

    // ICE 候选事件
    this.pc.onicecandidate = (event) => {
      if (event.candidate) {
        const ws = getWebSocket()
        ws?.sendMessage({
          msgType: MessageType.PEER,
          receiverId: this.remoteUserId,
          msgContent: {
            type: 'candidate',
            candidate: event.candidate
          }
        })
      }
    }

    // 远程流事件
    this.pc.ontrack = (event) => {
      if (event.streams && event.streams[0]) {
        this.onRemoteStreamCallback?.(event.streams[0])
      }
    }

    // 连接状态变化
    this.pc.onconnectionstatechange = () => {
      console.log('连接状态:', this.pc?.connectionState)
    }

    // ICE 连接状态变化
    this.pc.oniceconnectionstatechange = () => {
      console.log('ICE 连接状态:', this.pc?.iceConnectionState)
    }
  }

  /**
   * 添加本地流
   */
  addLocalStream(stream: MediaStream): void {
    this.localStream = stream
    stream.getTracks().forEach((track) => {
      this.pc?.addTrack(track, stream)
    })
  }

  /**
   * 创建 Offer
   */
  async createOffer(): Promise<RTCSessionDescriptionInit> {
    const offer = await this.pc?.createOffer()
    if (offer) {
      await this.pc?.setLocalDescription(offer)
      return offer
    }
    throw new Error('创建 Offer 失败')
  }

  /**
   * 创建 Answer
   */
  async createAnswer(): Promise<RTCSessionDescriptionInit> {
    const answer = await this.pc?.createAnswer()
    if (answer) {
      await this.pc?.setLocalDescription(answer)
      return answer
    }
    throw new Error('创建 Answer 失败')
  }

  /**
   * 设置远程描述
   */
  async setRemoteDescription(description: RTCSessionDescriptionInit): Promise<void> {
    await this.pc?.setRemoteDescription(new RTCSessionDescription(description))
  }

  /**
   * 添加 ICE 候选
   */
  async addIceCandidate(candidate: RTCIceCandidateInit): Promise<void> {
    await this.pc?.addIceCandidate(new RTCIceCandidate(candidate))
  }

  /**
   * 设置远程流回调
   */
  onRemoteStream(callback: (stream: MediaStream) => void): void {
    this.onRemoteStreamCallback = callback
  }

  /**
   * 关闭连接
   */
  close(): void {
    this.pc?.close()
    this.pc = null
  }

  /**
   * 获取连接状态
   */
  getConnectionState(): RTCPeerConnectionState | undefined {
    return this.pc?.connectionState
  }
}

/**
 * WebRTC 管理器
 */
export class WebRTCManager {
  private peerConnections: Map<string, WebRTCPeerConnection> = new Map()
  private localStream: MediaStream | null = null

  /**
   * 获取本地媒体流
   */
  async getLocalStream(constraints: MediaStreamConstraints = { video: true, audio: true }): Promise<MediaStream> {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia(constraints)
      return this.localStream
    } catch (error) {
      console.error('获取媒体流失败:', error)
      throw error
    }
  }

  /**
   * 获取屏幕共享流
   */
  async getScreenStream(): Promise<MediaStream> {
    try {
      // @ts-ignore - Electron 支持
      return await navigator.mediaDevices.getDisplayMedia({
        video: {
          cursor: 'always'
        },
        audio: false
      })
    } catch (error) {
      console.error('获取屏幕共享失败:', error)
      throw error
    }
  }

  /**
   * 创建对等连接
   */
  createPeerConnection(remoteUserId: string): WebRTCPeerConnection {
    const pc = new WebRTCPeerConnection(remoteUserId)
    if (this.localStream) {
      pc.addLocalStream(this.localStream)
    }
    this.peerConnections.set(remoteUserId, pc)
    return pc
  }

  /**
   * 获取对等连接
   */
  getPeerConnection(remoteUserId: string): WebRTCPeerConnection | undefined {
    return this.peerConnections.get(remoteUserId)
  }

  /**
   * 关闭对等连接
   */
  closePeerConnection(remoteUserId: string): void {
    const pc = this.peerConnections.get(remoteUserId)
    if (pc) {
      pc.close()
      this.peerConnections.delete(remoteUserId)
    }
  }

  /**
   * 关闭所有连接
   */
  closeAllConnections(): void {
    this.peerConnections.forEach((pc) => pc.close())
    this.peerConnections.clear()
  }

  /**
   * 停止本地流
   */
  stopLocalStream(): void {
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => track.stop())
      this.localStream = null
    }
  }

  /**
   * 切换音频状态
   */
  toggleAudio(enabled: boolean): void {
    if (this.localStream) {
      this.localStream.getAudioTracks().forEach((track) => {
        track.enabled = enabled
      })
    }
  }

  /**
   * 切换视频状态
   */
  toggleVideo(enabled: boolean): void {
    if (this.localStream) {
      this.localStream.getVideoTracks().forEach((track) => {
        track.enabled = enabled
      })
    }
  }

  /**
   * 销毁管理器
   */
  destroy(): void {
    this.closeAllConnections()
    this.stopLocalStream()
  }
}

// 单例实例
let webrtcInstance: WebRTCManager | null = null

/**
 * 获取 WebRTC 管理器实例
 */
export function getWebRTCManager(): WebRTCManager {
  if (!webrtcInstance) {
    webrtcInstance = new WebRTCManager()
  }
  return webrtcInstance
}

/**
 * 销毁 WebRTC 管理器
 */
export function destroyWebRTCManager(): void {
  if (webrtcInstance) {
    webrtcInstance.destroy()
    webrtcInstance = null
  }
}
