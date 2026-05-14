// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:dotos/main.dart';
import 'package:dotos/widget_selection_screen.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  Future<void> pumpSelectionScreen(
    WidgetTester tester,
    String category,
    String title,
  ) async {
    await tester.binding.setSurfaceSize(const Size(390, 600));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(
      MaterialApp(
        home: WidgetSelectionScreen(
          category: category,
          title: title,
          onToggleTheme: () {},
        ),
      ),
    );
    await tester.pump();
  }

  testWidgets('Dashboard search filters widget categories', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(
      MaterialApp(home: DashboardScreen(onToggleTheme: () {})),
    );

    expect(find.byType(TextField), findsOneWidget);
    expect(find.text('CLOCK'), findsWidgets);

    await tester.enterText(find.byType(TextField), 'battery');
    await tester.pump();

    expect(find.text('BATTERY'), findsOneWidget);
    expect(find.text('CALENDAR'), findsNothing);
  });

  testWidgets('Calendar widget previews fit without overflow', (
    WidgetTester tester,
  ) async {
    await pumpSelectionScreen(tester, 'calendar', 'Calendar');

    expect(find.text('Day Focus'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('Music widget previews mount and remain scrollable', (
    WidgetTester tester,
  ) async {
    await pumpSelectionScreen(tester, 'music', 'Music');

    expect(find.text('Ring'), findsOneWidget);

    await tester.drag(find.byType(GridView), const Offset(0, -240));
    await tester.pumpAndSettle();

    expect(tester.takeException(), isNull);
  });

  testWidgets('Screen time widget previews mount cleanly', (
    WidgetTester tester,
  ) async {
    await pumpSelectionScreen(tester, 'screen_time', 'Screen Time');

    expect(find.text('Minimal'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
