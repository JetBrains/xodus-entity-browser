import React, {Component, ReactNode} from "react";
import {EntityLink, EntityView} from "../../api/backend-types";
import {
  Chip,
  ExpansionPanel, ExpansionPanelDetails, ExpansionPanelSummary,
  Grid,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography
} from "@material-ui/core";
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

interface EntityListViewProps {
  entity: EntityView
}

class EntityListView extends Component<EntityListViewProps> {

  state = {
    linksExpanded: false
  }

  constructor(props: EntityListViewProps) {
    super(props)
  }

  private static renderProperties(entity: EntityView) {
    if (!entity.properties.length) {
      return (
          <div>
            No properties
          </div>
      )
    }
    return (<ExpansionPanel square TransitionProps={{unmountOnExit: true}} defaultExpanded={true}>
          <ExpansionPanelSummary
              aria-controls="panel1d-content"
              id={"entity-properties-" + entity.id}
              expandIcon={<ExpandMoreIcon/>}>
            <Typography variant={"h6"}>Properties</Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell align="right">Type</TableCell>
                  <TableCell>Value</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {entity.properties.map((property) => (
                    <TableRow key={property.name}>
                      <TableCell component="th" scope="row">
                        {property.name}
                      </TableCell>
                      <TableCell align="right">{property.type.displayName}</TableCell>
                      <TableCell>{property.value}</TableCell>
                    </TableRow>
                ))}
              </TableBody>
            </Table>
          </ExpansionPanelDetails>
        </ExpansionPanel>
    )
  }

  private static renderLinks(entity: EntityView) {
    if (!entity.links.length) {
      return (<div>
        No links
      </div>)
    }
    return (<ExpansionPanel square TransitionProps={{unmountOnExit: true}}>
          <ExpansionPanelSummary
              aria-controls="panel1d-content"
              id={"entity-links-" + entity.id}
              expandIcon={<ExpandMoreIcon/>}>
            <Typography>Links</Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Value</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {entity.links.map((link) => (
                    <TableRow key={link.name}>
                      <TableCell component="th" scope="row">
                        {link.name}
                      </TableCell>
                      <TableCell>
                        {link.entities.map((entityLink: EntityLink) => (
                            <Chip size="small" clickable label={entityLink.label}
                                  color={entityLink.notExists ? "secondary" : "default"}/>
                        ))}
                      </TableCell>
                    </TableRow>
                ))}
              </TableBody>
            </Table>
          </ExpansionPanelDetails>
        </ExpansionPanel>
    )
  }

  render() {
    const entity = this.props.entity
    return (
        <Paper key={entity.id}>
          <Grid container spacing={2}>
            <Grid container item xs={12}>
              <Grid item xs={8}>
                <Typography variant={"h6"}>{entity.label}</Typography>
              </Grid>
              <Grid item xs={4}>
                view/edit
              </Grid>
              <Grid item xs={12}>
                {EntityListView.renderProperties(entity)}
              </Grid>
              <Grid item xs={12}>
                {EntityListView.renderLinks(entity)}
              </Grid>
            </Grid>
          </Grid>
        </Paper>
    )
  }
}

export default EntityListView
