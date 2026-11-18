# MQTT Topics

All topics use the `ABS/` prefix.

The state formats below assume `HOME_ASSISTANT_DISCOVERY=true` (the default).
When discovery is disabled, `ABS/partition/{id}` and `ABS/sensor/{id}` publish
human-readable summaries, and separate zone bypass topics are not published.

## Published Topics (Bridge → MQTT)

- `ABS/availability` — Bridge online/offline status (Last Will):
  `online` or `offline`.
- `ABS/conn` — Connection status: `Status: Connected` or
  `Status: Disconnected`.
- `ABS/partition/{id}` — Partition arming state. For `id=0` (global), possible
  values are `disarmed`, `armed_away`, `armed_custom_bypass`, `triggered`,
  `not_available`, `arming`, and `disarming`. Other IDs also support
  `armed_home` and `armed_night`.
- `ABS/partition/{id}/readiness` — Arming readiness (`0` aggregates assigned
  partitions). States include `ready`, `ready_to_force`, `not_ready`, `armed`,
  `arming`, `disarming`, `triggered`, and `unknown`. The aggregate can also be
  `partially_armed`.
- `ABS/sensor/{id}` — Zone/sensor open state: `ON` or `OFF`.
- `ABS/sensor/{id}_bypass` — Zone bypass state: `ON` or `OFF`.
- `ABS/panel_faults` — Panel fault status (text, such as `No Problems`).
- `ABS/panel_faults/attributes` — Panel fault details (JSON).
- `ABS/bridge_alerts` — Bridge alert state: `Warning!` or `System OK`.
- `ABS/bridge_alerts/last` — Last bridge alert message (text).
- `ABS/bridge_alerts/attributes` — Bridge alert details (JSON).

Home Assistant button discovery configs reference these state topics, but the
bridge does not publish state messages to them:

- `ABS/mode/{A|B|C|D}`
- `ABS/reset_alarm_memory`
- `ABS/reset_bridge_alerts`

The JSON attributes topics use these English keys:

- `ABS/panel_faults/attributes`: `faults` and `alarms_in_memory` arrays.
- `ABS/bridge_alerts/attributes`: an `alerts` array. Each alert has `Time` and
  `Message` fields.

`ABS/partition/0/readiness` aggregates readiness across assigned partitions.
It first reports `arming` or `disarming` during transitions, or `triggered` if
the global or any assigned partition has triggered. Otherwise, it reports
`unknown` if no partitions are assigned or any state is unknown, `not_ready`
if any partition is not ready, or `ready_to_force` if any partition requires a
forced arm. It reports `armed` when all partitions are armed,
`partially_armed` when some are armed and the others are ready, and `ready`
when all partitions are disarmed and can arm normally. `ready_to_force` is
informational; the bridge does not automatically send a forced-arm command.

## Command Topics (MQTT → Bridge)

- `ABS/partition/{id}/set` — Arm or disarm a partition. For `id=0` (global),
  accepted values are `DISARM` and `ARM_AWAY`. Other IDs also accept
  `ARM_HOME` and `ARM_NIGHT`.
- `ABS/sensor/{id}/set` — Toggle zone bypass:
  `ON` (bypass) or `OFF` (unbypass).
- `ABS/mode/{A|B|C|D}/set` — Activate an arming mode:
  `MODE_A`, `MODE_B`, `MODE_C`, or `MODE_D`.
- `ABS/absoluta_alarm_memory/set` — Reset alarm memory with
  `RESET_ALARM_MEMORY`.
- `ABS/absoluta_bridge_alerts/set` — Clear bridge alerts with `RESET_ERRORS`.
