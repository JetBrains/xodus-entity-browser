import {EntityLink} from '../../api/backend-types';
import {inject, observer} from 'mobx-react';
import * as React from 'react';
import {Component} from 'react';
import {DatabaseApi} from '../../api/api';

export type LinkProps = {
  dbApi: DatabaseApi,
  entity: EntityLink
}

@observer
@inject('routing')
export class EntityLinkWrapper extends Component<LinkProps> {

  private navigate() {
    const {dbApi, entity} = this.props;
    // @ts-ignore
    this.props.routing.push(`/databases/${dbApi.databaseUuid}/${entity.id}`);
  }

  render(): React.ReactNode {
    return (
      <span onClick={() => this.navigate()}>
        {this.props.children}
      </span>
    )
  }
}
