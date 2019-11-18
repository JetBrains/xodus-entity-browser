import React from 'react';
import {observer} from 'mobx-react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  LinearProgress
} from "@material-ui/core";
import {confirmationStore} from "./confirmation.store";


@observer
export class ConfirmationWrapper extends React.Component {

  state = {
    loading: false
  };

  render() {
    const dismiss = () => {
      confirmationStore.show = false;
      confirmationStore.action = undefined;
      confirmationStore.message = undefined;
      confirmationStore.title = undefined;
    };

    const confirm = async () => {
      const action = confirmationStore.action;
      if (action) {
        this.setState({loading: true});
        try {
          await action();
        } finally {
          this.setState({loading: false});
        }
      }
      dismiss();
    };

    return (
        <div>
          <Dialog
              open={confirmationStore.show}
              onClose={dismiss}
              fullWidth
              maxWidth={"md"}
              aria-labelledby="alert-dialog-title"
              aria-describedby="alert-dialog-description"
          >
            {confirmationStore.title &&
            <DialogTitle id="confirmation-dialog-title">{confirmationStore.title}</DialogTitle>}
            <DialogContent>
              {this.state.loading && <LinearProgress/>}
              <DialogContentText id="confirmation-dialog-description">
              {confirmationStore.message}
              </DialogContentText>
            </DialogContent>
            <DialogActions>
              <Button onClick={dismiss} color="default" variant="contained" disabled={this.state.loading}>
                Dismiss
              </Button>
              <Button onClick={confirm} color="primary" variant="contained" disabled={this.state.loading}>
                Confirm
              </Button>
            </DialogActions>
          </Dialog>
        </div>
    );
  }
}
