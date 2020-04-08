import React, {Component} from "react";
import IconButton from "@material-ui/core/IconButton/IconButton";
import MoreVertIcon from '@material-ui/icons/MoreVert'
import MenuItem from "@material-ui/core/MenuItem/MenuItem";
import Menu from "@material-ui/core/Menu/Menu";

interface ActionsDropdownState {
  anchor: EventTarget | null
}

interface MenuDropdownAction {
  title: string,
  action: (event: any) => void
}

interface ActionsDropdownProps {
  actions: Array<MenuDropdownAction>
}

class ActionsDropdown extends Component<ActionsDropdownProps, ActionsDropdownState> {

  state = {
    anchor: null
  };

  openDropdown = (event: any) => {
    this.setState({ anchor: event.currentTarget });
  };

  closeDropdown = () => {
    this.setState({ anchor: null });
  };

  doActionAndClose = (action: MenuDropdownAction) =>
    (event: any) => {
      action.action(event);
      this.closeDropdown();
    };

  render() {
    const getKey = (action : MenuDropdownAction) =>
      action.title.toLowerCase().replace(
        new RegExp('[^a-z]', 'g'), '-'
      );

    return (
      <div>
        <IconButton onClick={this.openDropdown}>
          <MoreVertIcon/>
        </IconButton>
        <Menu
          anchorEl={this.state.anchor}
          open={Boolean(this.state.anchor)}
          onClose={this.closeDropdown}
          disableAutoFocusItem
        >
          {this.props.actions.map(action => (
            <MenuItem
              key={`action-${getKey(action)}`}
              onClick={this.doActionAndClose(action)}
            >
              {action.title}
            </MenuItem>
          ))}
        </Menu>
      </div>
    );
  }
}

export default ActionsDropdown;
