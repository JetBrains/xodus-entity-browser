import React from 'react';
import {createStyles, ThemeProvider, withStyles, WithStyles,} from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import {observer} from "mobx-react";
import theme from "./theme";

const styles = () => createStyles({
  root: {
    display: 'flex',
    minHeight: '100vh',
  },
  app: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
  }
});

interface AppLayoutProps extends WithStyles<typeof styles> {
  classes: {
    root: string,
    app: string
  }
}

@observer
class AppLayout extends React.Component<AppLayoutProps> {

  render() {
    const {classes, children} = this.props;

    return (
        <ThemeProvider theme={theme}>
          <div className={classes.root}>
            <CssBaseline/>
            <div className={classes.app}>
              <main>
                {children}
              </main>
            </div>
          </div>
        </ThemeProvider>
    );
  }
}

export default withStyles(styles)(AppLayout);
