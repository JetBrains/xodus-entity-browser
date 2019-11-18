import React, {Component} from "react";
import {observer} from 'mobx-react';
import store from "../store/store";
import {LinearProgress} from "@material-ui/core";

@observer
class BasePage<P> extends Component<P> {

  state = {
    loading: false
  };

  pageId = '';

  componentDidMount() {
    return this.reload();
  }

  load = async () => {
    if (this.state.loading) {
      return;
    }
    this.syncPage();
    this.setState({loading: true});
    await this.doLoad();
    this.syncPage();
    this.setState({loading: false});
  };

  syncPage() {
    store.pageId = this.pageId;
  }

  async doLoad() {
    return;
  }

  reload = () => {
    this.clear();
    return this.load();
  };

  clear() {
  }

  canEdit = () => {
    return !store.readonly;
  };


  render() {
    if (this.state.loading) {
      return (
          <div>
            <br/>
            <LinearProgress color="secondary" variant="query"/>
          </div>
      )
    }
    return this.renderContent();
  }

  renderContent() {
    return (
        <div>
        </div>
    )
  }
}

export default BasePage;
