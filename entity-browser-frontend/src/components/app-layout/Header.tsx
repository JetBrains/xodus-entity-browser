// import React from 'react';
// import AppBar from '@material-ui/core/AppBar';
// import Grid from '@material-ui/core/Grid';
// import Hidden from '@material-ui/core/Hidden';
// import IconButton from '@material-ui/core/IconButton';
// import MenuIcon from '@material-ui/icons/Menu';
// import Toolbar from '@material-ui/core/Toolbar';
// import Typography from '@material-ui/core/Typography';
// import {Theme} from '@material-ui/core/styles/createMuiTheme';
// import withStyles, {WithStyles} from "@material-ui/core/styles/withStyles";
// import createStyles from "@material-ui/core/styles/createStyles";
// import store from "../../store/store";
//
// const lightColor = 'rgba(255, 255, 255, 0.7)';
//
// const styles = (theme: Theme) => createStyles({
//   gridSpacing: {
//     margin: -theme.spacing(3),
//   },
//   secondaryBar: {
//     zIndex: 0,
//   },
//   menuButton: {
//     marginLeft: theme.spacing(-1),
//   },
//   link: {
//     textDecoration: 'none',
//     color: theme.palette.text.primary,
//     '&:hover': {
//       color: theme.palette.common.white,
//     },
//   },
//   button: {
//     borderColor: lightColor,
//   }
// });
//
// interface HeaderProps extends WithStyles<typeof styles> {
//   classes: Record<"menuButton" | "link" | "secondaryBar" | "button" | "gridSpacing", string>;
//
//   onDrawerToggle?: React.ReactEventHandler<{}>;
//   pageTitle: string
// }
//
// function Header(props: HeaderProps) {
//   const {classes, onDrawerToggle, pageTitle} = props;
//
//   const user = store.currentUser;
//   return (
//       <React.Fragment>
//         <AppBar color="primary" position="sticky" elevation={0}>
//           <Toolbar>
//             <Grid container spacing={8} alignItems="center" className={classes.gridSpacing}>
//               <Hidden mdUp>
//                 <Grid item>
//                   <IconButton
//                       color="inherit"
//                       aria-label="Open drawer"
//                       onClick={onDrawerToggle}
//                       className={classes.menuButton}
//                   >
//                     <MenuIcon/>
//                   </IconButton>
//                 </Grid>
//               </Hidden>
//               <Grid item xs>
//                 <Typography color="inherit" variant="h5">
//                   {pageTitle}
//                 </Typography>
//               </Grid>
//             </Grid>
//           </Toolbar>
//         </AppBar>
//       </React.Fragment>
//   );
// }
//
//
// export default withStyles(styles)(Header);
