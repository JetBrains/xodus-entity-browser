import React from 'react';
import {createStyles, ThemeProvider, withStyles, WithStyles,} from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import Header from './Header';
import {observer} from "mobx-react";
import store from "../../store/store";
import {Theme} from "@material-ui/core/styles/createMuiTheme";
import theme from "./theme";

const styles = (theme: Theme) => createStyles({
  root: {
    display: 'flex',
    minHeight: '100vh',
  },
  app: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
  },
  main: {
    flex: 1,
    padding: theme.spacing(3, 3),
    background: '#eaeff1',
  }
});

interface AppLayoutProps extends WithStyles<typeof styles> {
  classes: {
    root: string,
    app: string,
    main: string
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
            <Header pageTitle={store.pageId}/>
            <main className={classes.main}>
              {children}
            </main>
          </div>
        </div>
      </ThemeProvider>
    );
  }
}

export default withStyles(styles)(AppLayout);
