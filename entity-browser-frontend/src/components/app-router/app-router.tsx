import React, {Component} from "react";
import {Route, RouteComponentProps, Switch, withRouter} from 'react-router-dom'
import {inject, observer} from "mobx-react";

import store from '../../store/store';
import NotFoundPage from '../../pages/errors/NotFoundPage';
import api from "../../api/api";
import EmptyStore from "../../pages/errors/EmptyStore";
import DatabasePage from "../../pages/DatabasePage";
import EntityPage from "../../pages/EntityPage";

@observer
class AppRouter extends Component<RouteComponentProps> {

  async componentDidMount() {
    const state = await api.system.state();
    store.readonly = state.readonly;
    store.databases = state.dbs;
  }

  render() {
    if (store.databases.length) {
      return (
          <Switch>
            <Route exact path='/' component={DatabasePage}/>
            <Route exact path='/databases/:databaseId'  component={DatabasePage}/>
            <Route exact path='/entities' component={EntityPage}/>
            <Route component={NotFoundPage}/>
          </Switch>
      );
    } else {
      return (
          <Switch>
            <Route component={EmptyStore}/>
          </Switch>
      );
    }
  }
}

export default withRouter(AppRouter);
