import React, {Component} from "react";
import {observer} from 'mobx-react';
import {LinearProgress} from "@material-ui/core";
import Header from "../components/app-layout/Header";
import {RouteComponentProps} from "react-router-dom";

@observer
class BasePage extends Component<RouteComponentProps> {

  state = {
    loading: false,
    title: ''
  };

  componentDidMount() {
    return this.reload();
  }

  load = async () => {
    if (this.state.loading) {
      return;
    }
    this.setState({loading: true});
    await this.doLoad();
    this.setState({loading: false});
  };

  withTitle(title: string) {
    this.setState({title: title})
  }

  async doLoad() {
    return;
  }

  reload = () => {
    return this.load();
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

    const title = this.state.title
    return (
        <div>
          <Header pageTitle={title}/>
          <div className={"main"}>
            {this.renderContent()}
          </div>
        </div>
    );
  }

  renderContent() {
    return (
        <div>
        </div>
    )
  }
}

export default BasePage;
