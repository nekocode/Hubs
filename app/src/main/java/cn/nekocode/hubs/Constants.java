/*
 * Copyright (C) 2017 nekocode (nekocode.cn@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cn.nekocode.hubs;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public interface Constants {
    String ACTION_PREFIX = "cn.nekocode.hubs.action.";

    /**
     * You can run the following command to refresh a hub page immediately.
     * adb shell "am broadcast -a cn.nekocode.hubs.action.NOTIFY_HUB_INSTALLED -e hub_id '$hub_id'"
     */
    String ACTION_NOTIFY_HUB_INSTALLED = ACTION_PREFIX + "NOTIFY_HUB_INSTALLED";
    String ACTION_NOTIFY_HUB_UNINSTALLED = ACTION_PREFIX + "NOTIFY_HUB_UNINSTALLED";
    String ACTION_NOTIFY_HUB_PREFERENCE_CHANGED = ACTION_PREFIX + "NOTIFY_HUB_PREFERENCE_CHANGED";


    String ARG_HUB = "hub";
    String ARG_HUBS = "hubs";
    String ARG_HUB_ID = "hub_id";
}
