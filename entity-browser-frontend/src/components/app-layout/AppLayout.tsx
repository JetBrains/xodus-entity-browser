import React from 'react';
import {ThemeProvider} from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import Hidden from '@material-ui/core/Hidden';
import withStyles, {WithStyles} from "@material-ui/core/styles/withStyles";
import createStyles from "@material-ui/core/styles/createStyles";
import {observer} from "mobx-react";
import Navigator from "./Navigator";

import store from '../../store/store';

import theme from './theme';

const drawerWidth = 256;

const styles = createStyles({
  root: {
    display: 'flex',
    minHeight: '100vh',
  },
  drawer: {
    [theme.breakpoints.up('md')]: {
      width: drawerWidth,
      flexShrink: 0,
    },
  },
  appContent: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
  },
  mainContent: {
    flex: 1,
    background: '#efeff3',
    padding: theme.spacing()
  }
});

interface AppLayoutProps extends WithStyles<typeof styles> {
  classes: {
    root: string,
    drawer: string,
    appContent: string,
    mainContent: string
  }
}

type AppLayoutState = {
  mobileOpen: boolean
};

@observer
class AppLayout extends React.Component<AppLayoutProps, AppLayoutState> {
  state = {
    mobileOpen: false,
  };

  handleDrawerToggle = () => {
    this.setState(state => ({mobileOpen: !state.mobileOpen}));
  };

  render() {
    const {classes, children} = this.props;

    return (
      <ThemeProvider theme={theme}>
        <div className={classes.root}>
          <CssBaseline/>
          <nav className={classes.drawer}>
            <Hidden mdUp implementation="js">
              <Navigator
                PaperProps={{style: {width: drawerWidth}}}
                variant="temporary"
                open={this.state.mobileOpen}
                onClose={this.handleDrawerToggle}
                pageId={store.pageId}
              />
            </Hidden>
            <Hidden smDown implementation="css">
              <Navigator
                PaperProps={{style: {width: drawerWidth}}}
                pageId={store.pageId}
              />
            </Hidden>
          </nav>
          <div className={classes.appContent}>
            <main className={classes.mainContent}>
              {children}
            </main>
          </div>
        </div>
      </ThemeProvider>
    );
  }
}

export default withStyles(styles)(AppLayout);
