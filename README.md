# CK Pay — Android starter

This is a UI/prototype starter for the CK Pay order workflow.

Flow:
1. User selects Buy/Sell.
2. Creates an order.
3. Payment details screen displays the configured recipient details (demo placeholders).
4. User marks payment as made and selects a screenshot from the device.
5. Order becomes Pending.
6. Admin verification is intentionally a backend feature to add with Firebase/secure server rules.

Important:
- Replace demo payment details before any real use.
- Do not treat a screenshot alone as proof of payment; verify the actual transaction.
- Add authentication, a secure backend, storage rules, audit logs, and applicable payment/compliance requirements before production.

Open with Android Studio and run on an emulator/device.
