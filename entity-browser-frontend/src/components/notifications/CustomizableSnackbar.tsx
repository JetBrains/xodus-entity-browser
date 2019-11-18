import React from 'react';
import classNames from 'classnames';

import {Theme, withStyles} from '@material-ui/core/styles';
import Slide from '@material-ui/core/Slide';

import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import ErrorIcon from '@material-ui/icons/Error';
import InfoIcon from '@material-ui/icons/Info';
import CloseIcon from '@material-ui/icons/Close';
import green from '@material-ui/core/colors/green';
import amber from '@material-ui/core/colors/amber';
import IconButton from '@material-ui/core/IconButton';
import Snackbar from '@material-ui/core/Snackbar';
import SnackbarContent from '@material-ui/core/SnackbarContent';
import WarningIcon from '@material-ui/icons/Warning';

const variantIcon:{[key: string]: any} = {
  success: CheckCircleIcon,
  warning: WarningIcon,
  error: ErrorIcon,
  info: InfoIcon,
};

const snackStyles = (theme: Theme) => ({
  success: {
    backgroundColor: green[600],
  },
  error: {
    backgroundColor: theme.palette.error.dark,
  },
  info: {
    backgroundColor: theme.palette.primary.dark,
  },
  warning: {
    backgroundColor: amber[700],
  },
  icon: {
    fontSize: 20,
  },
  iconVariant: {
    opacity: 0.9,
    marginRight: theme.spacing(),
  },
  message: {
    display: 'flex',
    alignItems: 'center',
  },
});

function Transition(props: any) {
  return <Slide {...props} direction="left" />;
}


interface SnackbarProps  {
  classes?: any,
  message: string,
  onClose: (event: React.SyntheticEvent, reason: string) => void,
  ttl: number,
  variant: string,
  open: boolean,
  style: Object
}

const CustomizableSnackbar: React.FC<SnackbarProps> = (props) => {
  const { classes, message, onClose, variant = 'info', ttl = 5000, ...other } = props;
  const Icon = variantIcon[variant];

  return (
    <Snackbar
      {...other}
      onClose={onClose}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      autoHideDuration={ttl}
      TransitionComponent={Transition}
      transitionDuration={250}
    >
      <SnackbarContent
        className={classNames(classes[variant])}
        aria-describedby="client-snackbar"
        message={
          <span id="client-snackbar" className={classes.message}>
          <Icon className={classNames(classes.icon, classes.iconVariant)} />
            {message}
        </span>
        }
        action={[
          // @ts-ignore
          <IconButton
            key="close"
            aria-label="Close"
            color="inherit"
            className={classes.close}
            onClick={onClose}
          >
            <CloseIcon className={classes.icon} />
          </IconButton>,
        ]}
      />
    </Snackbar>
  );
};

export default withStyles(snackStyles)(CustomizableSnackbar);
