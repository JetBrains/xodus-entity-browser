import {EntityLink, LinkPager} from '../../api/backend-types';
import {inject, observer} from 'mobx-react';
import * as React from 'react';
import {Component, Fragment} from 'react';
import {EntityLinkWrapper} from './EntityLinkWrapper';
import {Chip} from '@material-ui/core';
import {DatabaseApi} from '../../api/api';

export type LinkProps = {
  pager: LinkPager,
  dbApi: DatabaseApi,
  editMode: boolean
}

@observer
export class EntityLinks extends Component<LinkProps> {

  constructor(props: LinkProps) {
    super(props);
  }

  render(): React.ReactNode {
    const pager = this.props.pager;
    return (
      <Fragment>
        {pager.entities.map((entityLink: EntityLink) => (
          <EntityLinkWrapper entity={entityLink} dbApi={this.props.dbApi}>
            <Chip size="small" clickable label={entityLink.label}
                  color={entityLink.notExists ? 'secondary' : 'default'} variant={'outlined'}/>
          </EntityLinkWrapper>
        ))}
      </Fragment>
    )
  }
}
