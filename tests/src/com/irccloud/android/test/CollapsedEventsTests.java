/*
 * Copyright (c) 2015 IRCCloud, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irccloud.android.test;

import android.test.AndroidTestCase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irccloud.android.CollapsedEventsList;
import com.irccloud.android.data.model.Event;
import com.irccloud.android.data.model.Server;

public class CollapsedEventsTests extends AndroidTestCase {
    private long eid = 1;

    private void addMode(CollapsedEventsList list, String mode, String nick, String from) {
        ArrayNode add = new ObjectMapper().createArrayNode();
        ObjectNode op = new ObjectMapper().createObjectNode();
        op.put("param", nick);
        op.put("mode", mode);
        add.add(op);

        Event e = new Event();
        e.eid = eid++;
        e.type = "user_channel_mode";
        e.from = from;
        e.from_mode = "q";
        e.nick = nick;
        e.target_mode = mode;
        e.server = "irc.example.net";
        e.chan = null;
        e.ops = new ObjectMapper().createObjectNode();
        ((ObjectNode)e.ops).put("add", add);
        ((ObjectNode)e.ops).put("remove", new ObjectMapper().createArrayNode());

        list.addEvent(e);
    }

    private void removeMode(CollapsedEventsList list, String mode, String nick, String from) {
        ArrayNode remove = new ObjectMapper().createArrayNode();
        ObjectNode op = new ObjectMapper().createObjectNode();
        op.put("param", nick);
        op.put("mode", mode);
        remove.add(op);

        Event e = new Event();
        e.eid = eid++;
        e.type = "user_channel_mode";
        e.from = from;
        e.nick = nick;
        e.server = "irc.example.net";
        e.chan = null;
        e.ops = new ObjectMapper().createObjectNode();
        ((ObjectNode)e.ops).put("add", new ObjectMapper().createArrayNode());
        ((ObjectNode)e.ops).put("remove", remove);

        list.addEvent(e);
    }

    public void testOper1() {
        CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "Y", "sam", "ChanServ");

        assertEquals("<b>\u0004E02305\u0002•\u000F sam</b> was promoted to oper (\u0004E02305+Y\u000F) by \u0004E7AA00\u0002•\u000F ChanServ", list.getCollapsedMessage());
    }

    public void testOper2() {
        CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "y", "sam", "ChanServ");

        assertEquals("<b>\u0004E02305\u0002•\u000F sam</b> was promoted to oper (\u0004E02305+y\u000F) by \u0004E7AA00\u0002•\u000F ChanServ", list.getCollapsedMessage());
    }

    public void testOwner1() {
        CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "q", "sam", "ChanServ");

        assertEquals("<b>\u0004E7AA00\u0002•\u000F sam</b> was promoted to owner (\u0004E7AA00+q\u000F) by \u0004E7AA00\u0002•\u000F ChanServ", list.getCollapsedMessage());
    }

    public void testOwner2() {
        CollapsedEventsList list = new CollapsedEventsList();
        Server s = new Server();
        s.MODE_OPER = "";
        s.MODE_OWNER = "y";
        list.setServer(s);
        addMode(list, "y", "sam", "ChanServ");

        assertEquals("<b>\u0004E7AA00\u0002•\u000F sam</b> was promoted to owner (\u0004E7AA00+y\u000F) by \u0002•\u000F ChanServ", list.getCollapsedMessage());
    }

    public void testOp() {
		CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "o", "sam", "ChanServ");

		assertEquals("<b>\u0004BA1719\u0002•\u000F sam</b> was opped (\u0004BA1719+o\u000F) by \u0004E7AA00\u0002•\u000F ChanServ", list.getCollapsedMessage());
	}
	
	public void testDeop() {
		CollapsedEventsList list = new CollapsedEventsList();
        removeMode(list, "o", "sam", "ChanServ");
		assertEquals("<b>sam</b> was de-opped (\u0004BA1719-o\u000F) by ChanServ", list.getCollapsedMessage());
	}
	
	public void testVoice() {
		CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "v", "sam", "ChanServ");
		assertEquals("<b>\u000425B100\u0002•\u000F sam</b> was voiced (\u000425B100+v\u000F) by \u0004E7AA00\u0002•\u000F ChanServ", list.getCollapsedMessage());
	}
	
	public void testDevoice() {
		CollapsedEventsList list = new CollapsedEventsList();
        removeMode(list, "v", "sam", "ChanServ");
		assertEquals("<b>sam</b> was de-voiced (\u000425B100-v\u000F) by ChanServ", list.getCollapsedMessage());
	}

    public void testOpByServer() {
        CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "o", "sam", null);

        assertEquals("<b>\u0004BA1719\u0002•\u000F sam</b> was opped (\u0004BA1719+o\u000F) by the server <b>irc.example.net</b>", list.getCollapsedMessage());
    }

    public void testOpDeop() {
        CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "o", "sam", "james");
        removeMode(list, "o", "sam", "ChanServ");

        assertEquals("<b>\u0004BA1719\u0002•\u000F sam</b> was de-opped (\u0004BA1719-o\u000F) by ChanServ", list.getCollapsedMessage());
    }

    public void testJoin() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		assertEquals("→ <b>sam</b> joined (sam@example.net)", list.getCollapsedMessage());
	}
	
	public void testPart() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_PART, "sam", null, "sam@example.net", null, null, null);
		assertEquals("← <b>sam</b> left (sam@example.net)", list.getCollapsedMessage());
	}
	
	public void testQuit() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, "Quit: leaving", null);
		assertEquals("⇐ <b>sam</b> quit (sam@example.net) Quit: leaving", list.getCollapsedMessage());
	}

    public void testQuit2() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, "*.net *.split", null);
        assertEquals("⇐ <b>sam</b> quit (sam@example.net) *.net *.split", list.getCollapsedMessage());
    }

    public void testNickChange() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
		assertEquals("sam_ → <b>sam</b>", list.getCollapsedMessage());
	}

    public void testNickChangeQuit() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam_", "sam", "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam_", null, "sam@example.net", null, "Bye!", null);
        assertEquals("⇐ <b>sam_</b> (was sam) quit (sam@example.net) Bye!", list.getCollapsedMessage());
    }

    public void testJoinQuit() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
		assertEquals("↔ <b>sam</b> popped in", list.getCollapsedMessage());
	}

    public void testJoinQuitJoin() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
        assertEquals("→ <b>sam</b> joined (sam@example.net)", list.getCollapsedMessage());
    }

    public void testJoinJoin() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "james", null, "james@example.net", null, null, null);
		assertEquals("→ <b>sam</b> and <b>james</b> joined", list.getCollapsedMessage());
	}

	public void testJoinQuit2() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "james", null, "james@example.net", null, null, null);
		assertEquals("→ <b>sam</b> joined ⇐ <b>james</b> quit", list.getCollapsedMessage());
	}

	public void testJoinPart() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_PART, "sam", null, "sam@example.net", null, null, null);
		assertEquals("↔ <b>sam</b> popped in", list.getCollapsedMessage());
	}

	public void testJoinPart2() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_PART, "james", null, "james@example.net", null, null, null);
		assertEquals("→ <b>sam</b> joined ← <b>james</b> left", list.getCollapsedMessage());
	}

	public void testQuitJoin() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		assertEquals("↔ <b>sam</b> nipped out", list.getCollapsedMessage());
	}

	public void testPartJoin() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_PART, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		assertEquals("↔ <b>sam</b> nipped out", list.getCollapsedMessage());
	}

	public void testJoinNickchange() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
		assertEquals("→ <b>sam</b> (was sam_) joined (sam@example.net)", list.getCollapsedMessage());
	}

	public void testQuitJoinNickchange() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam_", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
		assertEquals("↔ <b>sam</b> (was sam_) nipped out", list.getCollapsedMessage());
	}

	public void testQuitJoinNickchange2() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
		assertEquals("↔ <b>sam</b> nipped out", list.getCollapsedMessage());
	}

	public void testQuitJoinMode() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
        addMode(list, "o", "sam", "ChanServ");
		assertEquals("↔ <b>\u0004BA1719\u0002•\u000F sam</b> (opped) nipped out", list.getCollapsedMessage());
	}

	public void testQuitJoinModeNickPart() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam_", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
        addMode(list, "o", "sam_", "ChanServ");
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_PART, "sam", null, "sam@example.net", null, null, null);
		assertEquals("← <b>\u0004BA1719\u0002•\u000F sam</b> (was sam_; opped) left", list.getCollapsedMessage());
	}

	public void testNickchangeNickchange() {
		CollapsedEventsList list = new CollapsedEventsList();
        list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "james", "james_old", "james@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
		assertEquals("james_old → <b>james</b>, sam_ → <b>sam</b>", list.getCollapsedMessage());
	}

	public void testJoinQuitNickchange() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
		assertEquals("↔ <b>sam</b> (was sam_) nipped out", list.getCollapsedMessage());
	}

    public void testJoinQuitNickchange2() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam_", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
        assertEquals("↔ <b>sam</b> (was sam_) nipped out", list.getCollapsedMessage());
    }

    public void testModeMode() {
		CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "v", "sam", "ChanServ");
        addMode(list, "o", "james", "ChanServ");
		assertEquals("mode: <b>\u0004BA1719\u0002•\u000F james</b> (opped) and <b>\u000425B100\u0002•\u000F sam</b> (voiced)", list.getCollapsedMessage());
	}

    public void testModeMode2() {
        CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "o", "sam", "ChanServ");
        addMode(list, "v", "sam", "ChanServ");
        assertEquals("mode: <b>\u0004BA1719\u0002•\u000F sam</b> (opped, voiced)", list.getCollapsedMessage());
    }

    public void testModeNickchange() {
		CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "o", "james", "ChanServ");
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_", "sam@example.net", null, null, null);
		assertEquals("mode: <b>\u0004BA1719\u0002•\u000F james</b> (opped) • sam_ → <b>sam</b>", list.getCollapsedMessage());
	}
	
	public void testJoinMode() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
        addMode(list, "o", "sam", "ChanServ");
		assertEquals("→ <b>\u0004BA1719\u0002•\u000F sam</b> (opped) joined", list.getCollapsedMessage());
	}

    public void testJoinModeMode() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
        addMode(list, "o", "sam", "ChanServ");
        addMode(list, "q", "sam", "ChanServ");
        assertEquals("→ <b>\u0004E7AA00\u0002•\u000F sam</b> (promoted to owner, opped) joined", list.getCollapsedMessage());
    }

    public void testModeJoinPart() {
		CollapsedEventsList list = new CollapsedEventsList();
        addMode(list, "o", "james", "ChanServ");
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_PART, "sam", null, "sam@example.net", null, null, null);
		assertEquals("mode: <b>\u0004BA1719\u0002•\u000F james</b> (opped) ↔ <b>sam</b> popped in", list.getCollapsedMessage());
	}
	
	public void testJoinNickchangeModeModeMode() {
		CollapsedEventsList list = new CollapsedEventsList();
		list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
		list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "james", "james_old", "james@example.net", null, null, null);
        removeMode(list, "o", "james", "ChanServ");
        addMode(list, "v", "RJ", "ChanServ");
        addMode(list, "v", "james", "ChanServ");
		assertEquals("→ <b>sam</b> joined • mode: <b>\u000425B100\u0002•\u000F RJ</b> (voiced) • james_old → <b>\u000425B100\u0002•\u000F james</b> (voiced, de-opped)", list.getCollapsedMessage());
	}

    public void testMultiChannelJoin() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.showChan = true;
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test1");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test2");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test3");
        assertEquals("→ <b>sam</b> joined #test1, #test2, and #test3", list.getCollapsedMessage());
    }

    public void testMultiChannelNickChangeQuitJoin() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.showChan = true;
        list.addEvent(eid++, CollapsedEventsList.TYPE_NICKCHANGE, "sam", "sam_old", "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test1");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test2");
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test1");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test2");
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test1");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test2");
        assertEquals("↔ <b>sam</b> (was sam_old) nipped out #test1 and #test2", list.getCollapsedMessage());
    }

    public void testMultiChannelPopIn1() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.showChan = true;
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test1");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test2");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test3");
        list.addEvent(eid++, CollapsedEventsList.TYPE_PART, "sam", null, "sam@example.net", null, null, "#test1");
        list.addEvent(eid++, CollapsedEventsList.TYPE_PART, "sam", null, "sam@example.net", null, null, "#test2");
        assertEquals("→ <b>sam</b> joined #test3 ↔ <b>sam</b> popped in #test1 and #test2", list.getCollapsedMessage());
    }

    public void testMultiChannelPopIn2() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.showChan = true;
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test1");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test2");
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test3");
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        assertEquals("↔ <b>sam</b> popped in #test1, #test2, and #test3", list.getCollapsedMessage());
    }

    public void testMultiChannelQuit() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.showChan = true;
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, "#test1");
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, null, null);
        assertEquals("⇐ <b>sam</b> quit (sam@example.net) ", list.getCollapsedMessage());
    }

    public void testNetSplit() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "sam", null, "sam@example.net", null, "irc.example.net irc2.example.net", null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "james", null, "james@example.net", null, "irc.example.net irc2.example.net", null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "RJ", null, "RJ@example.net", null, "irc3.example.net irc2.example.net", null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_QUIT, "russ", null, "russ@example.net", null, "fake.net fake.net", null);
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "sam", null, "sam@example.net", null, null, null);
        assertEquals("irc.example.net ↮ irc2.example.net and irc3.example.net ↮ irc2.example.net ⇐ <b>russ</b> quit", list.getCollapsedMessage());
    }

    public void testChanServJoin() {
        CollapsedEventsList list = new CollapsedEventsList();
        list.addEvent(eid++, CollapsedEventsList.TYPE_JOIN, "ChanServ", null, "ChanServ@services.", null, null, null);
        addMode(list, "o", "ChanServ", null);
        assertEquals("→ <b>\u0004BA1719\u0002•\u000F ChanServ</b> (opped) joined", list.getCollapsedMessage());
    }
}
