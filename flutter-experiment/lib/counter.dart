import 'package:flutter/material.dart';

class CounterPage extends StatefulWidget {
  const CounterPage({Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return CounterPageState();
  }
}

class CounterPageState extends State<CounterPage> {
  int _counter = 0;

  void _addOne() {
    setState(() {
      ++_counter;
    });
  }

  void _subtractOne() {
    setState(() {
      --_counter;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
            title: const Text('Counter')
        ),
        body: Column(
          children: [
            Text(
                'The counter is $_counter',
                style: Theme.of(context).textTheme.bodyText1
            ),
            Row(
                children: [
                  TextButton(
                      onPressed: () {
                        debugPrint('plus one (currently: $_counter)');
                        _addOne();
                      },
                      child: Text('+1')
                  ),
                  TextButton(
                      onPressed: () {
                        debugPrint('minus one (currently: $_counter)');
                        _subtractOne();
                      },
                      child: Text('-1')
                  )
                ]
            )
          ],
        )
    );
  }
}
