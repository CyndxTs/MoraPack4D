import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import router from './router'
import './styles/main.scss'
import { DataProvider } from './dataProvider'

import { initOperationManager } from './services/operationManager';

initOperationManager({ wsEndpoint: '/ws' });

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <DataProvider>
      <RouterProvider router={router} />
    </DataProvider>
  </React.StrictMode>,
)
