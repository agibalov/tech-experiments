import 'package:flutter/material.dart';
import 'package:flutter_experiment/counter.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('Counter', () {
    testWidgets('should start at 0', (WidgetTester tester) async {
      await tester.pumpWidget(const MaterialApp(home: CounterPage()));
      expect(find.text('The counter is 0'), findsOneWidget);
    });

    testWidgets('can increment', (WidgetTester tester) async {
      await tester.pumpWidget(const MaterialApp(home: CounterPage()));

      expect(find.text('The counter is 0'), findsOneWidget);
      expect(find.text('+1'), findsOneWidget);

      await tester.tap(find.text('+1'));
      await tester.pump();

      expect(find.text('The counter is 1'), findsOneWidget);
    });

    testWidgets('can decrement', (WidgetTester tester) async {
      await tester.pumpWidget(const MaterialApp(home: CounterPage()));

      expect(find.text('The counter is 0'), findsOneWidget);
      expect(find.text('-1'), findsOneWidget);

      await tester.tap(find.text('-1'));
      await tester.pump();

      expect(find.text('The counter is -1'), findsOneWidget);
    });
  });
}
