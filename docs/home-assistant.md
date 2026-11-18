# Home Assistant Integration

## MQTT Discovery

When `HOME_ASSISTANT_DISCOVERY = true` (the default), the bridge automatically publishes MQTT Discovery payloads so that Home Assistant detects all entities without any manual configuration.

Discovered entities include:

- **Alarm control panels** — one per partition (arm away, arm home, arm night, disarm)
- **Sensors** — readiness for each partition and the aggregate global partition (`ready`, `ready_to_force`, `not_ready`, `armed`, `partially_armed`, `arming`, `disarming`, `triggered`, or `unknown`)
- **Binary sensors** — one per zone (open/closed)
- **Switches** — zone bypass toggles
- **Buttons** — arming mode selectors (A, B, C, D), alarm memory reset, bridge alert reset
- **Sensors** — bridge alerts, panel faults

The bridge listens on `homeassistant/status` and re-publishes discovery payloads automatically whenever Home Assistant restarts.

---

## Dashboard

Below are ready-to-use card templates. A complete single-file template is also available in [dashboard_template](../dashboard_template).

Home Assistant may retain existing entity IDs in its entity registry after an upgrade because discovery `unique_id` values are stable. The examples below use the English IDs; if your registry still has the previous IDs, rename the entities in Home Assistant or update the dashboard references. Find entity IDs in **Settings → Devices & Services → MQTT → your device** or **Developer Tools → States**.

### Required HACS custom cards

- [card-mod](https://github.com/thomasloven/lovelace-card-mod) — card styling
- [auto-entities](https://github.com/thomasloven/lovelace-auto-entities) — dynamic entity lists
- [fold-entity-row](https://github.com/thomasloven/lovelace-fold-entity-row) — collapsible partition rows
- [multiple-entity-row](https://github.com/benct/lovelace-multiple-entity-row) — zone rows with inline bypass toggle

---

### Card 1 — Panel faults and alarm memory

Shown only when there is an active fault or alarm in memory. Includes a button to clear the alarm memory (with confirmation).

```yaml
- type: conditional
  conditions:
    - condition: state
      entity: sensor.absoluta_panel_panel_faults
      state_not: No Problems
    - condition: state
      entity: sensor.absoluta_panel_panel_faults
      state_not: unavailable
  card:
    type: horizontal-stack
    cards:
      - type: markdown
        title: Alarm System Alarms
        content: >-
          {% set faults =
          state_attr('sensor.absoluta_panel_panel_faults',
          'faults') | default([], true) %}
          {% set alarms =
          state_attr('sensor.absoluta_panel_panel_faults',
          'alarms_in_memory') | default([], true) %}
          {% for g in faults %} - **{{ g.Time }}** — {{ g.Message }}
          {% endfor %}
          {% for a in alarms %} - **{{ a.Time }}** — {{ a.Message }} *(memory)*
          {% endfor %}
        card_mod:
          style: |
            ha-card {
              background: transparent !important;
              box-shadow: none !important;
              border: none !important;
            }
      - type: button
        entity: button.absoluta_panel_reset_alarm_memory
        icon: mdi:delete-sweep
        show_name: false
        show_state: false
        tap_action:
          action: perform-action
          perform_action: button.press
          target:
            entity_id: button.absoluta_panel_reset_alarm_memory
          confirmation: true
        card_mod:
          style: |
            :host {
              flex: 0 0 5rem !important;
              align-self: flex-start !important;
              margin-top: 1rem !important;
            }
            ha-card {
              background: transparent !important;
              box-shadow: none !important;
              border: none !important;
            }
    card_mod:
      style: |
        :host {
          background: var(--ha-card-background, white);
          border-radius: var(--ha-card-border-radius, 12px);
          box-shadow: var(--ha-card-box-shadow, 0 2px 2px rgba(0,0,0,.14));
          border: var(--ha-card-border-width, 1px) solid var(--ha-card-border-color, transparent);
          overflow: hidden;
        }
```

---

### Card 2 — Bridge alerts

Shown only when the bridge has raised an alert (e.g. connection loss). Includes a button to dismiss.

```yaml
- type: conditional
  conditions:
    - condition: state
      entity: sensor.absoluta_panel_bridge_alerts
      state: Warning!
  card:
    type: horizontal-stack
    cards:
      - type: markdown
        title: Bridge Alerts
        content: >-
          {% set alerts =
          state_attr('sensor.absoluta_panel_bridge_alerts', 'alerts') |
          default([], true) %}
          {% for alert in alerts | reverse %} - **{{ alert.Time }}** — {{ alert.Message }}
          {% endfor %}
        card_mod:
          style: |
            ha-card {
              background: transparent !important;
              box-shadow: none !important;
              border: none !important;
            }
      - type: button
        entity: button.absoluta_panel_reset_bridge_alerts
        icon: mdi:delete-sweep
        show_name: false
        show_state: false
        tap_action:
          action: perform-action
          perform_action: button.press
          target:
            entity_id: button.absoluta_panel_reset_bridge_alerts
          confirmation: true
        card_mod:
          style: |
            :host {
              flex: 0 0 5rem !important;
              align-self: flex-start !important;
              margin-top: 1rem !important;
            }
            ha-card {
              background: transparent !important;
              box-shadow: none !important;
              border: none !important;
            }
    card_mod:
      style: |
        :host {
          background: var(--ha-card-background, white);
          border-radius: var(--ha-card-border-radius, 12px);
          box-shadow: var(--ha-card-box-shadow, 0 2px 2px rgba(0,0,0,.14));
          border: var(--ha-card-border-width, 1px) solid var(--ha-card-border-color, transparent);
          overflow: hidden;
        }
```

---

### Card 3 — Global alarm panel

Standard HA alarm panel card for the global partition (partition 0).

```yaml
- states:
    - arm_away
  type: alarm-panel
  entity: alarm_control_panel.absoluta_panel_global
```

---

### Card 4 — Arming mode buttons

One button per arming mode (A–D). Each press sends the corresponding mode command to the panel.

```yaml
- type: horizontal-stack
  cards:
    - show_name: true
      show_icon: true
      type: button
      tap_action:
        action: perform-action
        perform_action: button.press
        target:
          entity_id: button.absoluta_panel_mode_a
      icon_height: 50px
      entity: button.absoluta_panel_mode_a
      show_state: false
    - show_name: true
      show_icon: true
      type: button
      tap_action:
        action: perform-action
        perform_action: button.press
        target:
          entity_id: button.absoluta_panel_mode_b
      icon_height: 50px
      entity: button.absoluta_panel_mode_b
      show_state: false
    - show_name: true
      show_icon: true
      type: button
      tap_action:
        action: perform-action
        perform_action: button.press
        target:
          entity_id: button.absoluta_panel_mode_c
      icon_height: 50px
      entity: button.absoluta_panel_mode_c
      show_state: false
    - show_name: true
      show_icon: true
      type: button
      tap_action:
        action: perform-action
        perform_action: button.press
        target:
          entity_id: button.absoluta_panel_mode_d
      icon_height: 50px
      entity: button.absoluta_panel_mode_d
      show_state: false
```

---

### Card 5 — Active sensors

Automatically lists all zones that are currently open or triggered. Hidden when all zones are clear.

```yaml
- type: custom:auto-entities
  card:
    type: entities
    title: Active Alarm Zones
  filter:
    include:
      - entity_id: binary_sensor.absoluta_panel_*
        state: 'on'
    exclude: []
  show_empty: false
```

---

### Card 6 — Bypassed zones

Automatically lists all zones that are currently bypassed. Hidden when none are bypassed.

```yaml
- type: custom:auto-entities
  card:
    type: entities
    title: Bypassed Alarm Zones
    show_header_toggle: false
  filter:
    include:
      - entity_id: switch.absoluta_panel_*_bypass
        state: 'on'
    exclude: []
  show_empty: false
```

---

### Card 7 — Partitions with zones and bypass toggles

A collapsible list of partitions. Each partition expands to show its zones with an inline bypass switch. Duplicate the `fold-entity-row` block for each partition, and add one entry per zone inside it.

```yaml
- type: vertical-stack
  cards:
    - type: entities
      entities:

        # Partition 1 — replace entity IDs with your own
        - type: custom:fold-entity-row
          style:
            .: |
              div#head {
                --toggle-icon-width: 24px;
              }
          head:
            type: custom:multiple-entity-row
            entity: alarm_control_panel.absoluta_panel_partition_1  # REPLACE
          entities:
            - entity: binary_sensor.absoluta_panel_zone_1            # REPLACE
              type: custom:multiple-entity-row
              show_state: false
              state_color: true
              entities:
                - entity: switch.absoluta_panel_zone_1_bypass         # REPLACE
                  name: Bypass
                  toggle: true
            - entity: binary_sensor.absoluta_panel_zone_2            # REPLACE
              type: custom:multiple-entity-row
              show_state: false
              state_color: true
              entities:
                - entity: switch.absoluta_panel_zone_2_bypass         # REPLACE
                  name: Bypass
                  toggle: true

        # Partition 2 — duplicate this block for each additional partition
        - type: custom:fold-entity-row
          style:
            .: |
              div#head {
                --toggle-icon-width: 24px;
              }
          head:
            type: custom:multiple-entity-row
            entity: alarm_control_panel.absoluta_panel_partition_2  # REPLACE
          entities:
            - entity: binary_sensor.absoluta_panel_zone_3            # REPLACE
              type: custom:multiple-entity-row
              show_state: false
              state_color: true
              entities:
                - entity: switch.absoluta_panel_zone_3_bypass         # REPLACE
                  name: Bypass
                  toggle: true
            - entity: binary_sensor.absoluta_panel_zone_4            # REPLACE
              type: custom:multiple-entity-row
              show_state: false
              state_color: true
              entities:
                - entity: switch.absoluta_panel_zone_4_bypass         # REPLACE
                  name: Bypass
                  toggle: true
```
