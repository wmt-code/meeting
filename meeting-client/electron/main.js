const { app, BrowserWindow, ipcMain } = require('electron')
const path = require('path')

// 开发环境标识
const isDev = !app.isPackaged

let mainWindow = null

/**
 * 创建主窗口
 */
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 1024,
    minHeight: 600,
    show: false,
    frame: true,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js')
    }
  })

  // 加载应用
  if (isDev) {
    mainWindow.loadURL('http://localhost:5173')
    mainWindow.webContents.openDevTools()
  } else {
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }

  // 窗口准备好后显示
  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
  })

  // 窗口关闭
  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

/**
 * 应用就绪
 */
app.whenReady().then(() => {
  createWindow()

  // macOS 特性：点击 Dock 图标重新创建窗口
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

/**
 * 所有窗口关闭时退出（macOS 除外）
 */
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

/**
 * IPC 通信：最小化窗口
 */
ipcMain.on('window-minimize', () => {
  if (mainWindow) {
    mainWindow.minimize()
  }
})

/**
 * IPC 通信：最大化/还原窗口
 */
ipcMain.on('window-maximize', () => {
  if (mainWindow) {
    if (mainWindow.isMaximized()) {
      mainWindow.unmaximize()
    } else {
      mainWindow.maximize()
    }
  }
})

/**
 * IPC 通信：关闭窗口
 */
ipcMain.on('window-close', () => {
  if (mainWindow) {
    mainWindow.close()
  }
})

/**
 * IPC 通信：获取应用版本
 */
ipcMain.handle('get-app-version', () => {
  return app.getVersion()
})

/**
 * IPC 通信：获取用户数据路径
 */
ipcMain.handle('get-user-data-path', () => {
  return app.getPath('userData')
})
