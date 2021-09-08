import createMuiTheme from "@material-ui/core/styles/createTheme";

const baseTheme = createMuiTheme({
  typography: {
    // useNextVariants: true
  }
});

const theme = createMuiTheme({
  typography: {
    h5: {
      fontWeight: 500,
      fontSize: 26,
      letterSpacing: 0.5,
    },
    // useNextVariants: true
  },
  palette: {
    primary: {
      light: '#63ccff',
      main: '#009be5',
      dark: '#006db3'
    },
  },
  shape: {
    borderRadius: 8,
  },
  overrides: {
    MuiDrawer: {
      paper: {
        backgroundColor: '#24292e',
      },
    },
    MuiButton: {
      label: {
        textTransform: 'initial',
      },
      contained: {
        boxShadow: 'none',
        '&:active': {
          boxShadow: 'none',
        },
      },
    },
    MuiInput: {},
    MuiTabs: {
      root: {
        marginLeft: baseTheme.spacing(),
      },
      indicator: {
        height: 3,
        borderTopLeftRadius: 3,
        borderTopRightRadius: 3
      },
    },
    MuiTab: {
      root: {
        margin: '0 16px',
        minWidth: 0,
        padding: 0,
        [baseTheme.breakpoints.up('md')]: {
          padding: 0,
          minWidth: 0,
        },
      },
    },
    MuiIconButton: {
      root: {
        padding: baseTheme.spacing(),
      },
    },
    MuiTableCell: {
      root: {
        padding: baseTheme.spacing()
      }
    },
    MuiTooltip: {
      tooltip: {
        borderRadius: 4,
      },
    },
    MuiDivider: {
      root: {
        backgroundColor: '#404854',
      },
    },
    MuiListItemText: {
      primary: {
        fontWeight: baseTheme.typography.fontWeightMedium,
      },
    },
    MuiListItemIcon: {
      root: {
        color: 'inherit',
        marginRight: 0,
        '& svg': {
          fontSize: 20,
        },
      },
    },
    MuiAvatar: {
      root: {
        width: 32,
        height: 32,
      },
    },
    MuiFab: {
      root: {
        position: 'fixed',
        right: 32,
        bottom: 32
      }
    },
    MuiMenuItem: {
      root: {
        fontWeight: baseTheme.typography.fontWeightMedium,
        paddingTop: 4,
        paddingBottom: 4,
      }
    },
    MuiAppBar: {
      colorPrimary: {
        background: 'linear-gradient(to left, #232f3e, #18202c)',
        fontFamily: baseTheme.typography.fontFamily,
        fontSize: baseTheme.typography.fontSize,
        fontWeight: baseTheme.typography.fontWeightLight
      }
    },
    MuiTypography: {
      h5: {
        fontWeight: baseTheme.typography.fontWeightRegular
      }
    },
    MuiExpansionPanel: {
      root: {
        borderBottom: '1px solid transparent',
        borderTop: '1px solid transparent',
        marginBottom: -1,
        minHeight: baseTheme.spacing(4),
        boxShadow: 'none',
        '&:before': {
          opacity: 1
        },
        '&$expanded': {
          margin: 'auto',
          borderTop: `1px solid ${baseTheme.palette.divider}`
        }
      },

    },
    MuiExpansionPanelSummary: {
      content: {
        margin: `${baseTheme.spacing(0.5)}px 0`,
        '&$expanded': {
          margin: `${baseTheme.spacing(0.5)}px 0 !important`
        }
      }
    },
    MuiPaper: {
      rounded: {
        borderRadius: 0
      }
    },
    MuiDialogActions: {
      root: {
        padding: baseTheme.spacing(2)
      }
    }
  },
  props: {
    MuiTab: {
      disableRipple: true,
    },
  },
  mixins: {
    ...baseTheme.mixins,
    toolbar: {
      minHeight: 48,
    }
  },
  breakpoints: {
    values: {
      xs: 0, sm: 600, md: 1000, lg: 1280, xl: 1920
    }
  }
});

export default theme;
