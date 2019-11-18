import React, {SyntheticEvent} from 'react';
import {observer} from 'mobx-react';

import store from './notifications.store';

import CustomizableSnackbar from './CustomizableSnackbar';

@observer
export class NotificationsWrapper extends React.Component {
  handleClose = (id: number) => (event: SyntheticEvent, reason: string) => {
    if (reason === 'clickaway') {
      return;
    }

    store.hide(id);
  };

  render() {
    return (
        <div>
          {store.notifications.map((notification, index) => {
            return <CustomizableSnackbar
                variant={notification.type}
                key={notification.id}
                message={notification.message}
                open={notification.open}
                ttl={notification.ttl}
                onClose={this.handleClose(notification.id)}

                style={{marginBottom: index * 70}}
            />;
          })}
        </div>
    );
  }
}
