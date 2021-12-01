import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Andrey\'s app',
        theme: ThemeData(
            brightness: Brightness.dark
        ),
        initialRoute: '/',
        routes: {
          '/': (context) => const HomePage(),
          '/about': (context) => const AboutPage()
        }
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          title: const Text('Home') // TODO: what is const here?
      ),
      body: Column(
        children: [
          Text(
              'hello world',
              style: Theme.of(context).textTheme.bodyText1),
          Text(
              'hello world',
              style: Theme.of(context).textTheme.caption),
          TextButton(
              onPressed: () {
                var x = 123;
                debugPrint('Navigating to About $x');
                Navigator.pushNamed(context, '/about');
              },
              child: const Text('Go to About'))
        ]
      )
    );
  }
}

class AboutPage extends StatelessWidget {
  const AboutPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
            title: const Text('About')
        ),
        body: Text(
            'This is the about page',
            style: Theme.of(context).textTheme.bodyText1
        )
    );
  }
}