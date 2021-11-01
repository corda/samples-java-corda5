import React from "react";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import ListItemAvatar from "@mui/material/ListItemAvatar";
import Avatar from "@mui/material/Avatar";
import EmojiPeopleOutlinedIcon from "@mui/icons-material/EmojiPeopleOutlined";
import BusinessOutlinedIcon from "@mui/icons-material/BusinessOutlined";
import AccountBalanceOutlinedIcon from "@mui/icons-material/AccountBalanceOutlined";

const GroupMembers = () => {
  return (
    <List>
      <ListItem>
        <ListItemAvatar>
          <Avatar>
            <EmojiPeopleOutlinedIcon />
          </Avatar>
        </ListItemAvatar>
        <ListItemText
          primary="Peter"
          secondary="OU=INC, O=Peter, L=New York, C=US"
        />
      </ListItem>
      <ListItem>
        <ListItemAvatar>
          <Avatar>
            <BusinessOutlinedIcon />
          </Avatar>
        </ListItemAvatar>
        <ListItemText
          primary="Mars Express"
          secondary="OU=LLC, O=Mars Express, L=London, C=GB"
        />
      </ListItem>
      <ListItem>
        <ListItemAvatar>
          <Avatar>
            <AccountBalanceOutlinedIcon />
          </Avatar>
        </ListItemAvatar>
        <ListItemText primary="Notary" secondary="O=notary, L=London, C=GB" />
      </ListItem>
    </List>
  );
};

export default GroupMembers;
