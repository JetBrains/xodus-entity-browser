// @ts-ignore
import React, {Component} from 'react';
import clsx from 'clsx';
import Drawer from '@material-ui/core/Drawer';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import StorageIcon from '@material-ui/icons/Storage';
import {Link} from 'react-router-dom';


import {Theme} from '@material-ui/core/styles/createMuiTheme';
import withStyles, {WithStyles} from "@material-ui/core/styles/withStyles";
import createStyles from "@material-ui/core/styles/createStyles";
import {PaperProps} from "@material-ui/core/Paper";
import store from "../../store/store";
import AddIcon from '@material-ui/icons/Add';
import {observer} from "mobx-react";
import {Card, CardHeader} from "@material-ui/core";

export interface NavigatorClasses {
  title: string,
  item: string,
  itemCategory: string,
  itemPrimary: string,
  itemActionable: string,
  itemActiveItem: string,
  dense: string,
  categoriesMenu: string,
  navigationLink: string,
  jbLogo: string
}

interface NavigatorProps extends WithStyles<typeof styles> {
  classes: NavigatorClasses;

  open?: boolean;
  PaperProps?: Partial<PaperProps>;
  variant?: 'permanent' | 'persistent' | 'temporary';
  onClose?: React.ReactEventHandler<{}>;

  pageId: string
}

const styles = (theme: Theme) => createStyles({
  item: {
    paddingTop: 4,
    paddingBottom: 4,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  itemCategory: {
    backgroundColor: '#232f3e',
    boxShadow: '0 -1px 0 #404854 inset',
    paddingTop: 16,
    paddingBottom: 16,
  },
  title: {
    fontSize: theme.spacing(3),
    paddingLeft: theme.spacing(7),
    fontFamily: theme.typography.fontFamily,
    color: theme.palette.common.white,
    overflow: 'hidden',
    height: theme.spacing(10)
  },
  itemActionable: {
    '&:hover': {
      backgroundColor: 'rgba(255, 255, 255, 0.08)',
    },
  },
  itemActiveItem: {
    color: '#4fc3f7',
  },
  itemPrimary: {
    color: 'inherit',
    fontSize: theme.typography.fontSize,
    '&$dense': {
      fontSize: theme.typography.fontSize,
    },
  },
  dense: {},
  categoriesMenu: {
    marginTop: theme.spacing(2),
  },
  navigationLink: {
    textDecoration: 'none'
  },
  jbLogo: {
    color: 'black',
    position: 'absolute',
    top: -2 * theme.spacing(),
    left: -2 * theme.spacing()
  }
});

@observer
class Navigator extends Component<NavigatorProps> {

  render() {
    const {classes, pageId, ...other} = this.props;

    return (
        <Drawer variant="permanent" {...other}>
          <List disablePadding>
            <ListItem className={clsx(classes.title, classes.item, classes.itemCategory)}>
              Overview
            </ListItem>
            <div className={classes.categoriesMenu}>
              {store.databases.map((database) => (
                  <Link
                      to={`/databases/${database.uuid}`}
                      key={database.uuid}
                      className={classes.navigationLink}
                  >
                    <ListItem
                        button
                        className={clsx(
                            classes.item,
                            classes.itemActionable,
                        )}
                    >
                      <Card>
                        <CardHeader title={database.location}>
                        </CardHeader>
                      </Card>
                    </ListItem>
                  </Link>
              ))}
              {!store.readonly && <a className={classes.navigationLink}>
                <ListItem
                  button
                  className={clsx(
                      classes.item,
                      classes.itemActionable
                  )}
                >
                  <AddIcon/> database
                </ListItem>
              </a>}
            </div>
          </List>
        </Drawer>
    );
  }
}

export default withStyles(styles)(Navigator);
