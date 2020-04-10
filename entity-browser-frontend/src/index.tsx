import React from "react";
import {render} from "react-dom";
import {Router} from 'react-router-dom';
import {createBrowserHistory} from 'history';
import {syncHistoryWithStore} from 'mobx-react-router';

import AppRouter from './components/app-router/app-router';
import AppLayout from './components/app-layout/AppLayout';
import {NotificationsWrapper} from './components/notifications/notifications';
import routerStore from './store/router-store';

import './styles/styles.scss';
import {ConfirmationWrapper} from "./components/confirmation/ConfirmationWrapper";
import {Provider} from "mobx-react";

document.addEventListener('DOMContentLoaded', function () {
  // @ts-ignore
  const basename = AppBuildConfig.appContext;
  const history = syncHistoryWithStore(
      createBrowserHistory({basename}),
      routerStore
  );

  const stores = {
    // Key can be whatever you want
    routing: routerStore,
    // ...other stores
  };

  render(
      <div>
        <Provider {...stores}>
          <Router history={history}>
            <AppLayout>
              <AppRouter/>
              <NotificationsWrapper/>
              <ConfirmationWrapper/>
            </AppLayout>
          </Router>
        </Provider>
      </div>,
      document.getElementById('root')
  );
});
